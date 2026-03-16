import { B, deptColor, statusCfg } from "../data/constants";

export default function Dashboard({ empList, leaves, dashStats, approve, reject, setPage }) {

  // Use real API stats if available, otherwise fall back to empList counts
  const stats = [
    {
      lbl: "Total Employees",
      val: dashStats?.totalEmployees   ?? empList.length,
      sub: `+${dashStats?.newHiresThisMonth ?? 0} this month`,
      col: B.navy,
      ico: "👥",
    },
    {
      lbl: "In Office",
      val: dashStats?.inOfficeToday    ?? empList.filter(e => e.status === "office").length,
      sub: "QR verified today",
      col: B.orange,
      ico: "🏢",
    },
    {
      lbl: "Work from Home",
      val: dashStats?.wfhToday         ?? empList.filter(e => e.status === "wfh").length,
      sub: "WiFi auto-marked",
      col: B.blue,
      ico: "🏠",
    },
    {
      lbl: "On Leave",
      val: dashStats?.onLeaveToday     ?? empList.filter(e => e.status === "leave").length,
      sub: `${dashStats?.pendingLeaveRequests ?? 0} pending approval`,
      col: B.amber,
      ico: "🌴",
    },
  ];

  // Payroll trend from dashStats or empty
  const payrollTrend = dashStats?.payrollTrend || [];
  const maxPay = payrollTrend.length
    ? Math.max(...payrollTrend.map(p => p.totalNet || 0))
    : 1;

  // Map API employee to display shape
  const mapEmp = (e) => ({
    id:     e.id,
    name:   e.fullName || `${e.firstName} ${e.lastName}`,
    role:   e.designation,
    dept:   e.department || "Engineering",
    status: e.workMode === "WFH" ? "wfh" : e.status?.toLowerCase() === "active" ? "office" : "leave",
    av:     (e.fullName || `${e.firstName} ${e.lastName}`)
              .split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2),
  });

  const displayList = empList.slice(0, 5).map(mapEmp);

  return (
    <div className="ani">

      {/* Stat cards */}
      <div className="stats-grid">
        {stats.map(s => (
          <div key={s.lbl} className="stat-card">
            <div className="stat-stripe" style={{ background: s.col }} />
            <div className="stat-lbl">{s.lbl}</div>
            <div className="stat-val">{s.val}</div>
            <div className="stat-sub" style={{ color: s.col }}>{s.sub}</div>
            <div className="stat-ico">{s.ico}</div>
          </div>
        ))}
      </div>

      <div className="g2">
        {/* Recent employees */}
        <div className="card">
          <div className="card-hd">
            <span className="card-ttl">Recent Employees</span>
            <span className="card-act" onClick={() => setPage("employees")}>View all →</span>
          </div>
          <div className="card-div" />
          <table className="tbl">
            <thead>
              <tr><th>Employee</th><th>Department</th><th>Status</th></tr>
            </thead>
            <tbody>
              {displayList.map(e => {
                const dc = deptColor[e.dept] || { bg: "#FFF0E8", text: B.orange, dot: B.orange };
                const sc = statusCfg[e.status] || statusCfg["office"];
                return (
                  <tr key={e.id} className="tbl-row">
                    <td>
                      <div className="emp-wrap">
                        <div className="emp-av" style={{ background: dc.dot }}>{e.av}</div>
                        <div>
                          <div className="emp-nm">{e.name}</div>
                          <div className="emp-rl">{e.role}</div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className="dept-badge" style={{ background: dc.bg, color: dc.text }}>
                        {e.dept}
                      </span>
                    </td>
                    <td>
                      <span className="st-pill" style={{ background: sc.bg, color: sc.color }}>
                        <span className="st-dot-c" style={{ background: sc.dot }} />
                        {sc.label}
                      </span>
                    </td>
                  </tr>
                );
              })}
              {displayList.length === 0 && (
                <tr><td colSpan={3} className="empty">No employees found</td></tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Leave requests */}
        <div className="card">
          <div className="card-hd">
            <span className="card-ttl">Leave Requests</span>
            <span className="card-act" onClick={() => setPage("leave")}>Manage →</span>
          </div>
          <div className="card-div" />
          {leaves.filter(l => l.status === "PENDING").length === 0 && (
            <div className="empty">No pending requests 🎉</div>
          )}
          {leaves.filter(l => l.status === "PENDING").slice(0, 4).map(l => (
            <div key={l.id} className="lv-item">
              <div className="lv-av">
                {(l.employeeName || "").split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2)}
              </div>
              <div style={{ flex: 1 }}>
                <div className="lv-nm">{l.employeeName}</div>
                <div className="lv-tp">{l.leaveType} · {l.fromDate} – {l.toDate}</div>
                <div className="lv-dt">{l.numDays} working days</div>
              </div>
              <div className="lv-btns">
                <button className="btn-ok" onClick={() => approve(l.id)}>✓ Approve</button>
                <button className="btn-no" onClick={() => reject(l.id)}>✕</button>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="g2">
        {/* Payroll trend */}
        <div className="card">
          <div className="card-hd">
            <span className="card-ttl">Payroll Overview</span>
            <span className="card-act" onClick={() => setPage("payroll")}>Details →</span>
          </div>
          <div>
            <span className="sum-num">
              {dashStats?.payrollThisMonth
                ? `₹${(dashStats.payrollThisMonth / 100000).toFixed(2)}L`
                : "—"}
            </span>
            <span style={{ fontSize: 13, color: B.muted, marginLeft: 8 }}>this month</span>
          </div>
          {payrollTrend.length > 0 ? (
            <div className="pay-wrap">
              {payrollTrend.map((p, i) => (
                <div key={p.month} className="pay-bw">
                  <div
                    className="pay-b"
                    style={{
                      height: `${(p.totalNet / maxPay) * 60}px`,
                      opacity: i === payrollTrend.length - 1 ? 1 : 0.55,
                    }}
                  />
                  <span className="pay-lbl">
                    {p.month?.split(" ")[0]?.slice(0, 3)}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty" style={{ padding: 20 }}>No payroll data yet</div>
          )}
        </div>

        {/* Dept breakdown */}
        <div className="card">
          <div className="card-hd">
            <span className="card-ttl">Department Split</span>
            <span className="card-act" onClick={() => setPage("employees")}>View →</span>
          </div>
          <div className="card-div" />
          {(dashStats?.deptBreakdown || []).map(d => {
            const dc  = deptColor[d.department] || { bg: "#FFF0E8", text: B.orange };
            const pct = dashStats?.activeEmployees
              ? Math.round((d.count / dashStats.activeEmployees) * 100)
              : 0;
            return (
              <div key={d.department} style={{ marginBottom: 10 }}>
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
                  <span style={{ fontSize: 12, color: B.navy, fontWeight: 500 }}>
                    {d.department}
                  </span>
                  <span style={{ fontSize: 12, color: B.muted }}>
                    {d.count} · {pct}%
                  </span>
                </div>
                <div className="prog-wrap">
                  <div className="prog-bar" style={{ width: `${pct}%`, background: dc.text }} />
                </div>
              </div>
            );
          })}
          {(!dashStats?.deptBreakdown?.length) && (
            <div className="empty" style={{ padding: 16 }}>No data yet</div>
          )}
        </div>
      </div>
    </div>
  );
}
