import { useState, useEffect } from "react";
import { B, deptColor } from "../data/constants";
import { payrollApi } from "../utils/api";

const CURRENT_MONTH = new Date().toISOString().slice(0, 7); // "2026-03"

export default function Payroll({ empList, user }) {
  const [payrollList, setPayrollList] = useState([]);
  const [summary,     setSummary]     = useState(null);
  const [loading,     setLoading]     = useState(false);
  const [running,     setRunning]     = useState(false);
  const [error,       setError]       = useState("");
  const [success,     setSuccess]     = useState("");

  // ── Load payroll data ────────────────────────────────────
  const loadPayroll = async () => {
    setLoading(true);
    setError("");
    try {
      const [listRes, summRes] = await Promise.all([
        payrollApi.getByMonth(CURRENT_MONTH),
        payrollApi.getSummary(CURRENT_MONTH),
      ]);
      setPayrollList(Array.isArray(listRes.data) ? listRes.data : []);
      setSummary(summRes.data || null);
    } catch (e) {
      setError("Failed to load payroll: " + e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadPayroll(); }, []);

  // ── Run payroll ──────────────────────────────────────────
  const runPayroll = async () => {
    setRunning(true);
    setError("");
    setSuccess("");
    try {
      await payrollApi.process({
        payrollMonth:   CURRENT_MONTH + "-01",
        processedById:  user?.employeeId || 1,
      });
      setSuccess("Payroll processed successfully for all employees!");
      await loadPayroll();
    } catch (e) {
      setError("Payroll run failed: " + e.message);
    } finally {
      setRunning(false);
    }
  };

  const fmt = (v) => v != null ? `₹${(v / 1000).toFixed(0)}K` : "—";

  const summaryCards = [
    {
      lbl: "Total Payroll",
      val: summary?.totalNet != null ? `₹${(summary.totalNet / 100000).toFixed(2)}L` : "—",
      sub: new Date().toLocaleString("en-IN", { month: "long", year: "numeric" }),
      col: B.orange,
    },
    {
      lbl: "Processed",
      val: summary ? `${summary.processed}/${summary.totalEmployees}` : `0/${empList.length}`,
      sub: "employees",
      col: B.green,
    },
    {
      lbl: "Avg Net Salary",
      val: summary?.totalNet && summary?.processed
        ? `₹${Math.round(summary.totalNet / summary.processed / 1000)}K`
        : "—",
      sub: "per employee",
      col: B.navy,
    },
    {
      lbl: "Pending",
      val: summary?.draft ?? 0,
      sub: "awaiting processing",
      col: B.amber,
    },
  ];

  return (
    <div className="ani">
      <div className="hdr-row">
        <div>
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <span className="sum-num">
              {summary?.totalNet != null
                ? `₹${(summary.totalNet / 100000).toFixed(2)}L`
                : "—"}
            </span>
            {summary?.totalNet && <span className="sum-badge">Processed</span>}
          </div>
          <div style={{ fontSize: 13, color: B.muted, marginTop: 4 }}>
            Total payroll · {new Date().toLocaleString("en-IN", { month: "long", year: "numeric" })}
          </div>
          <div className="ou" />
        </div>
        <button
          className="btn-orange"
          onClick={runPayroll}
          disabled={running}
          style={{ opacity: running ? 0.7 : 1 }}
        >
          {running ? "⏳ Processing..." : "▶ Run Payroll"}
        </button>
      </div>

      {error   && <div style={{ background: "#fee2e2", border: "1px solid #fecaca", borderRadius: 9, padding: "10px 14px", fontSize: 13, color: "#dc2626", marginBottom: 16 }}>{error}</div>}
      {success && <div style={{ background: B.greenBg, border: "1px solid #bbf7d0",   borderRadius: 9, padding: "10px 14px", fontSize: 13, color: B.green, marginBottom: 16 }}>{success}</div>}

      {/* Summary stat cards */}
      <div className="stats-grid">
        {summaryCards.map(s => (
          <div key={s.lbl} className="stat-card">
            <div className="stat-stripe" style={{ background: s.col }} />
            <div className="stat-lbl">{s.lbl}</div>
            <div className="stat-val" style={{ fontSize: 26 }}>{s.val}</div>
            <div className="stat-sub" style={{ color: s.col }}>{s.sub}</div>
          </div>
        ))}
      </div>

      {/* Payroll table */}
      <div className="card">
        <div className="card-hd">
          <span className="card-ttl">Employee Payroll Breakdown</span>
          {loading && <span style={{ fontSize: 12, color: B.muted }}>Loading...</span>}
        </div>
        <div className="card-div" />
        <table className="tbl">
          <thead>
            <tr>
              <th>Employee</th>
              <th>Basic</th>
              <th>HRA</th>
              <th>Gross</th>
              <th>PF</th>
              <th>Tax</th>
              <th>Net Pay</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {payrollList.map(p => {
              const dc = deptColor[p.department] || { dot: B.orange };
              const av = (p.employeeName || "")
                .split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2);
              return (
                <tr key={p.id} className="tbl-row">
                  <td>
                    <div className="emp-wrap">
                      <div className="emp-av" style={{ background: dc.dot, width: 30, height: 30, borderRadius: 8, fontSize: 11 }}>{av}</div>
                      <div>
                        <div className="emp-nm">{p.employeeName}</div>
                        <div className="emp-rl">{p.department}</div>
                      </div>
                    </div>
                  </td>
                  <td style={{ fontSize: 13, color: B.navy,  fontWeight: 500 }}>{fmt(p.basicSalary)}</td>
                  <td style={{ fontSize: 13, color: B.green, fontWeight: 500 }}>+{fmt(p.hra)}</td>
                  <td style={{ fontSize: 13, color: B.navy,  fontWeight: 500 }}>{fmt(p.grossSalary)}</td>
                  <td style={{ fontSize: 13, color: B.red,   fontWeight: 500 }}>-{fmt(p.pfDeduction)}</td>
                  <td style={{ fontSize: 13, color: B.red,   fontWeight: 500 }}>-{fmt(p.incomeTax)}</td>
                  <td style={{ fontSize: 14, color: B.orange, fontWeight: 700 }}>{fmt(p.netSalary)}</td>
                  <td>
                    {p.status === "PAID"      && <span className="badge-ok">✓ Paid</span>}
                    {p.status === "PROCESSED" && <span className="badge-pend">⏳ Processed</span>}
                    {p.status === "DRAFT"     && <span style={{ fontSize: 11, color: B.muted }}>Draft</span>}
                  </td>
                </tr>
              );
            })}
            {payrollList.length === 0 && !loading && (
              <tr>
                <td colSpan={8} className="empty">
                  No payroll processed yet. Click "Run Payroll" to process.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
