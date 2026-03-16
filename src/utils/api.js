// ── Base URL from .env ─────────────────────────────────────
const BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081/api/v1";

// ── Auth token helpers ─────────────────────────────────────
export const getToken    = () => localStorage.getItem("ems_token") || "";
export const setToken    = (t) => localStorage.setItem("ems_token", t);
export const removeToken = () => localStorage.removeItem("ems_token");

const headers = () => ({
  "Content-Type": "application/json",
  ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {}),
});

// ── Generic fetch wrapper ──────────────────────────────────
async function request(method, path, body) {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: headers(),
    ...(body ? { body: JSON.stringify(body) } : {}),
  });

  // Token expired → redirect to login
  if (res.status === 401) {
    removeToken();
    window.location.reload();
    return;
  }

  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || "Request failed");
  }
  return res.json();
}

// ── Auth ───────────────────────────────────────────────────
export const authApi = {
  login:          (data)         => request("POST", "/auth/login", data),
  refresh:        (refreshToken) => request("POST", "/auth/refresh", { refreshToken }),
  changePassword: (data)         => request("POST", "/auth/change-password", data),
};

// ── Employees ──────────────────────────────────────────────
export const employeeApi = {
  getAll: (params = {}) => {
    const q = new URLSearchParams(
      Object.fromEntries(Object.entries(params).filter(([, v]) => v != null && v !== ""))
    ).toString();
    return request("GET", `/employees${q ? "?" + q : ""}`);
  },
  getById:      (id)       => request("GET",    `/employees/${id}`),
  create:       (data)     => request("POST",   "/employees", data),
  update:       (id, data) => request("PUT",    `/employees/${id}`, data),
  deactivate:   (id)       => request("DELETE", `/employees/${id}`),
  changeStatus: (id, s)    => request("PATCH",  `/employees/${id}/status?status=${s}`),
  getAttendanceSummary: (id, month) =>
    request("GET", `/employees/${id}/attendance-summary?month=${month}`),
};

// ── Attendance ─────────────────────────────────────────────
export const attendanceApi = {
  checkInQr:    (data)       => request("POST", "/attendance/checkin/qr",     data),
  checkInWifi:  (data)       => request("POST", "/attendance/checkin/wifi",   data),
  checkInManual:(data)       => request("POST", "/attendance/checkin/manual", data),
  checkOut:     (employeeId) => request("POST", "/attendance/checkout", { employeeId }),
  getToday:     ()           => request("GET",  "/attendance/today"),
  getTodayStats:()           => request("GET",  "/attendance/today/stats"),
  getHistory:   (params)     => {
    const q = new URLSearchParams(params).toString();
    return request("GET", `/attendance?${q}`);
  },
  getWeekly: (weekStart) =>
    request("GET", `/attendance/weekly?weekStart=${weekStart}`),
  getMonthlySummary: (id, month) =>
    request("GET", `/attendance/summary/${id}?month=${month}`),
};

// ── Leave ──────────────────────────────────────────────────
export const leaveApi = {
  apply:   (data)      => request("POST", "/leaves", data),
  approve: (id, data)  => request("PUT",  `/leaves/${id}/approve`, data),
  reject:  (id, data)  => request("PUT",  `/leaves/${id}/reject`,  data),
  cancel:  (id)        => request("PUT",  `/leaves/${id}/cancel`),
  getAll:  (params={}) => {
    const q = new URLSearchParams(
      Object.fromEntries(Object.entries(params).filter(([, v]) => v != null && v !== ""))
    ).toString();
    return request("GET", `/leaves${q ? "?" + q : ""}`);
  },
  getBalance: (id) => request("GET", `/leaves/${id}/balance`),
  getTypes:   ()   => request("GET", "/leaves/types"),
};

// ── Payroll ────────────────────────────────────────────────
export const payrollApi = {
  process:      (data)     => request("POST", "/payroll/process", data),
  getByMonth:   (month)    => request("GET",  `/payroll?month=${month}`),
  getSummary:   (month)    => request("GET",  `/payroll/summary?month=${month}`),
  getByEmployee:(id)       => request("GET",  `/payroll/employee/${id}`),
  markAsPaid:   (id, data) => request("PUT",  `/payroll/${id}/mark-paid`, data),
  taxPreview:   (salary)   => request("GET",  `/payroll/tax-preview?salary=${salary}`),
};

// ── Performance ────────────────────────────────────────────
export const performanceApi = {
  create:          (data)     => request("POST", "/performance", data),
  update:          (id, data) => request("PUT",  `/performance/${id}`, data),
  submit:          (id)       => request("PUT",  `/performance/${id}/submit`),
  acknowledge:     (id)       => request("PUT",  `/performance/${id}/acknowledge`),
  getByPeriod:     (period)   => request("GET",  `/performance?period=${period}`),
  getHistory:      (empId)    => request("GET",  `/performance/employee/${empId}/history`),
  getTopPerformers:(period)   => request("GET",  `/performance/top-performers?period=${period}`),
  getTrend:        ()         => request("GET",  "/performance"),
};

// ── Dashboard ──────────────────────────────────────────────
export const dashboardApi = {
  getStats: () => request("GET", "/dashboard/stats"),
};
