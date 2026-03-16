import { B } from "../data/constants";

const PAGE_TITLES = {
  dashboard:   "Dashboard",
  employees:   "Employees",
  attendance:  "Attendance",
  payroll:     "Payroll",
  performance: "Performance Reviews",
  leave:       "Leave Management",
};

export default function Topbar({ page, empCount, user, onLogout, dashStats }) {
  const today = new Date().toLocaleDateString("en-IN", {
    weekday: "long", day: "numeric", month: "long", year: "numeric",
  });

  const PAGE_SUBS = {
    dashboard:   `${today} · UniqueHire EMS`,
    employees:   `${empCount} team members`,
    attendance:  `In Office: ${dashStats?.inOfficeToday ?? "—"}  ·  WFH: ${dashStats?.wfhToday ?? "—"}  ·  On Leave: ${dashStats?.onLeaveToday ?? "—"}`,
    payroll:     `${new Date().toLocaleString("en-IN",{month:"long",year:"numeric"})} · Process on 28th`,
    performance: `Q1 ${new Date().getFullYear()} · Annual appraisal cycle`,
    leave:       `${dashStats?.pendingLeaveRequests ?? 0} pending · Review and manage leave applications`,
  };

  return (
    <div className="topbar">
      <div className="tb-left">
        <div className="page-title">{PAGE_TITLES[page]}</div>
        <div className="page-sub">{PAGE_SUBS[page]}</div>
      </div>
      <div className="tb-right">
        {/* Live WiFi / office indicator */}
        {dashStats && (
          <div className="wifi-pill">
            🏢 {dashStats.inOfficeToday} in office
          </div>
        )}
        {/* Logged-in user */}
        {user && (
          <div style={{
            display: "flex", alignItems: "center", gap: 8,
            background: B.offWhite, border: `1px solid ${B.border}`,
            borderRadius: 10, padding: "6px 12px",
            fontSize: 12, color: B.navy,
          }}>
            <div style={{
              width: 28, height: 28, borderRadius: 8,
              background: B.orange, color: "#fff",
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: 11, fontWeight: 700, flexShrink: 0,
            }}>
              {user.fullName?.split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase() || "AD"}
            </div>
            <div>
              <div style={{ fontWeight: 600, fontSize: 12 }}>{user.fullName}</div>
              <div style={{ fontSize: 10, color: B.muted }}>{user.role}</div>
            </div>
          </div>
        )}
        <div
          className="tb-btn"
          title="Logout"
          onClick={onLogout}
          style={{ fontSize: 14, cursor: "pointer" }}
        >
          🚪
        </div>
      </div>
    </div>
  );
}
