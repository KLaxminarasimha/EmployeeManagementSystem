import { useState, useEffect, useCallback } from "react";
import { FONTS } from "./data/constants";
import { CSS }   from "./styles/globalStyles";
import { getToken, removeToken } from "./utils/api";
import { employeeApi, leaveApi, dashboardApi } from "./utils/api";

import Login    from "./pages/Login";
import Sidebar  from "./components/Sidebar";
import Topbar   from "./components/Topbar";
import AddEmployeeModal from "./components/AddEmployeeModal";

import Dashboard   from "./pages/Dashboard";
import Employees   from "./pages/Employees";
import Attendance  from "./pages/Attendance";
import Payroll     from "./pages/Payroll";
import Performance from "./pages/Performance";
import Leave       from "./pages/Leave";

export default function App() {
  // ── Auth state ──────────────────────────────────────────
  const [user,    setUser]    = useState(() => {
    try { return JSON.parse(localStorage.getItem("ems_user")); }
    catch { return null; }
  });
  const isLoggedIn = !!user && !!getToken();

  // ── App state ───────────────────────────────────────────
  const [page,     setPage]    = useState("dashboard");
  const [modal,    setModal]   = useState(false);
  const [search,   setSearch]  = useState("");
  const [newE,     setNewE]    = useState({
    firstName: "", lastName: "", designation: "",
    departmentId: 1, email: "", phone: "",
    annualSalary: "", dateOfJoining: new Date().toISOString().split("T")[0],
  });

  // ── Data state ──────────────────────────────────────────
  const [empList,       setEmpList]       = useState([]);
  const [leaves,        setLeaves]        = useState([]);
  const [dashStats,     setDashStats]     = useState(null);
  const [loading,       setLoading]       = useState(false);
  const [error,         setError]         = useState("");

  // ── Load employees ───────────────────────────────────────
  const loadEmployees = useCallback(async () => {
    try {
      const res = await employeeApi.getAll({ page: 0, size: 50 });
      setEmpList(res.data?.employees || []);
    } catch (e) {
      setError("Failed to load employees: " + e.message);
    }
  }, []);

  // ── Load leaves ──────────────────────────────────────────
  const loadLeaves = useCallback(async () => {
    try {
      const res = await leaveApi.getAll();
      setLeaves(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      setError("Failed to load leaves: " + e.message);
    }
  }, []);

  // ── Load dashboard stats ─────────────────────────────────
  const loadDashStats = useCallback(async () => {
    try {
      const res = await dashboardApi.getStats();
      setDashStats(res.data);
    } catch (e) {
      setError("Failed to load dashboard stats: " + e.message);
    }
  }, []);

  // ── Fetch on login ───────────────────────────────────────
  useEffect(() => {
    if (!isLoggedIn) return;
    loadEmployees();
    loadLeaves();
    loadDashStats();
  }, [isLoggedIn, loadEmployees, loadLeaves, loadDashStats]);

  // ── Leave: approve ───────────────────────────────────────
  const approve = async (id) => {
    try {
      await leaveApi.approve(id, {
        reviewerId: user?.employeeId || 1,
        comment:    "Approved via EMS",
      });
      await loadLeaves();
      await loadDashStats();
    } catch (e) {
      setError("Approve failed: " + e.message);
    }
  };

  // ── Leave: reject ────────────────────────────────────────
  const reject = async (id) => {
    try {
      await leaveApi.reject(id, {
        reviewerId: user?.employeeId || 1,
        comment:    "Rejected via EMS",
      });
      await loadLeaves();
      await loadDashStats();
    } catch (e) {
      setError("Reject failed: " + e.message);
    }
  };

  // ── Add employee ─────────────────────────────────────────
  const addEmp = async () => {
    if (!newE.firstName.trim() || !newE.email.trim()) {
      setError("First name and email are required.");
      return;
    }
    setLoading(true);
    try {
      await employeeApi.create({
        ...newE,
        annualSalary: parseFloat(newE.annualSalary) || 600000,
      });
      setNewE({
        firstName: "", lastName: "", designation: "",
        departmentId: 1, email: "", phone: "",
        annualSalary: "", dateOfJoining: new Date().toISOString().split("T")[0],
      });
      setModal(false);
      await loadEmployees();
      await loadDashStats();
    } catch (e) {
      setError("Add employee failed: " + e.message);
    } finally {
      setLoading(false);
    }
  };

  // ── Logout ───────────────────────────────────────────────
  const logout = () => {
    removeToken();
    localStorage.removeItem("ems_user");
    setUser(null);
  };

  // ── Not logged in → show login ───────────────────────────
  if (!isLoggedIn) {
    return (
      <>
        <style>{FONTS}</style>
        <Login onLogin={(data) => {
          setUser(data);
        }} />
      </>
    );
  }

  return (
    <>
      <style>{FONTS}{CSS}</style>
      <div className="app">

        <Sidebar page={page} setPage={setPage} />

        <main className="main">
          <Topbar
            page={page}
            empCount={empList.length}
            user={user}
            onLogout={logout}
            dashStats={dashStats}
          />

          {/* Global error banner */}
          {error && (
            <div style={{
              margin: "0 36px", marginTop: 16,
              background: "#fee2e2", border: "1px solid #fecaca",
              borderRadius: 9, padding: "10px 16px",
              fontSize: 13, color: "#dc2626",
              display: "flex", justifyContent: "space-between", alignItems: "center",
            }}>
              <span>{error}</span>
              <button
                onClick={() => setError("")}
                style={{ background: "none", border: "none", cursor: "pointer", fontSize: 16, color: "#dc2626" }}
              >
                ✕
              </button>
            </div>
          )}

          <div className="body">
            {page === "dashboard" && (
              <Dashboard
                empList={empList}
                leaves={leaves}
                dashStats={dashStats}
                approve={approve}
                reject={reject}
                setPage={setPage}
              />
            )}
            {page === "employees" && (
              <Employees
                empList={empList}
                search={search}
                setSearch={setSearch}
                setModal={setModal}
                onRefresh={loadEmployees}
              />
            )}
            {page === "attendance"  && <Attendance  empList={empList} user={user} />}
            {page === "payroll"     && <Payroll     empList={empList} user={user} />}
            {page === "performance" && <Performance />}
            {page === "leave"       && (
              <Leave
                leaves={leaves}
                approve={approve}
                reject={reject}
                onRefresh={loadLeaves}
                user={user}
              />
            )}
          </div>
        </main>

        {modal && (
          <AddEmployeeModal
            newE={newE}
            setNewE={setNewE}
            onAdd={addEmp}
            onClose={() => setModal(false)}
            loading={loading}
          />
        )}

      </div>
    </>
  );
}
