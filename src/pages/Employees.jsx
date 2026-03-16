import { B, deptColor, statusCfg } from "../data/constants";

// Map API employee shape → display shape
const mapEmp = (e) => {
  const name = e.fullName || `${e.firstName || ""} ${e.lastName || ""}`.trim();
  const rawStatus = (e.workMode === "WFH" || e.workMode === "HYBRID")
    ? "wfh"
    : (e.status?.toLowerCase() !== "active" ? "leave" : "office");
  return {
    id:     e.id,
    name,
    role:   e.designation || "—",
    dept:   e.department   || "Engineering",
    status: rawStatus,
    av:     name.split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2),
    salary: e.monthlySalary || e.annualSalary / 12 || 0,
    rating: e.rating || "—",
    leave:  e.leaveBalance  ?? 0,
    email:  e.email,
  };
};

export default function Employees({ empList, search, setSearch, setModal }) {
  const displayList = empList.map(mapEmp);

  const filtered = displayList.filter(e =>
    [e.name, e.dept, e.role, e.email].some(v =>
      (v || "").toLowerCase().includes(search.toLowerCase())
    )
  );

  return (
    <div className="ani">
      <div className="hdr-row">
        <div>
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <span className="sum-num">{empList.length}</span>
            <span style={{ fontSize: 14, color: B.muted }}>team members at UniqueHire</span>
          </div>
          <div className="ou" />
        </div>
        <button className="btn-orange" onClick={() => setModal(true)}>＋ Add Employee</button>
      </div>

      <div className="srch-bar">
        <input
          className="srch-in"
          placeholder="🔍  Search by name, role, department..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <button className="filt-btn">All Depts ▾</button>
        <button className="filt-btn">All Status ▾</button>
      </div>

      <div className="card">
        <table className="tbl">
          <thead>
            <tr>
              <th>Employee</th>
              <th>Department</th>
              <th>Status</th>
              <th>Salary/Month</th>
              <th>Rating</th>
              <th>Leave Left</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(e => {
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
                  <td style={{ fontSize: 13, color: B.navy, fontWeight: 500 }}>
                    ₹{Math.round(e.salary / 1000)}K
                  </td>
                  <td style={{ fontSize: 13, color: B.orange, fontWeight: 600 }}>
                    {e.rating !== "—" ? `★ ${e.rating}` : "—"}
                  </td>
                  <td>
                    <div style={{ fontSize: 12, color: B.muted, marginBottom: 4 }}>
                      {e.leave} days
                    </div>
                    <div className="prog-wrap" style={{ width: 64 }}>
                      <div className="prog-bar" style={{ width: `${Math.min((e.leave / 18) * 100, 100)}%` }} />
                    </div>
                  </td>
                </tr>
              );
            })}
            {filtered.length === 0 && (
              <tr>
                <td colSpan={6} className="empty">
                  {search ? `No employees match "${search}"` : "No employees found"}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
