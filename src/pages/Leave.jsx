import { useState, useEffect } from "react";
import { B } from "../data/constants";
import { leaveApi } from "../utils/api";

export default function Leave({ leaves, approve, reject, onRefresh, user }) {
  const [types,   setTypes]   = useState([]);
  const [form,    setForm]    = useState({ employeeId: "", leaveTypeId: "", fromDate: "", toDate: "", reason: "" });
  const [applying, setApplying] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [error,   setError]   = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    leaveApi.getTypes().then(res => setTypes(res.data || [])).catch(() => {});
  }, []);

  const applyLeave = async () => {
    if (!form.employeeId || !form.leaveTypeId || !form.fromDate || !form.toDate) {
      setError("All fields are required."); return;
    }
    setApplying(true);
    setError(""); setSuccess("");
    try {
      await leaveApi.apply({
        employeeId:  parseInt(form.employeeId),
        leaveTypeId: parseInt(form.leaveTypeId),
        fromDate:    form.fromDate,
        toDate:      form.toDate,
        reason:      form.reason,
      });
      setSuccess("Leave applied successfully!");
      setForm({ employeeId: "", leaveTypeId: "", fromDate: "", toDate: "", reason: "" });
      setShowForm(false);
      onRefresh();
    } catch (e) {
      setError("Apply failed: " + e.message);
    } finally {
      setApplying(false);
    }
  };

  const pending  = leaves.filter(l => l.status === "PENDING");
  const approved = leaves.filter(l => l.status === "APPROVED");
  const rejected = leaves.filter(l => l.status === "REJECTED" || l.status === "CANCELLED");

  const summaryCards = [
    { lbl: "Total",    val: leaves.length,   col: B.navy  },
    { lbl: "Pending",  val: pending.length,  col: B.amber },
    { lbl: "Approved", val: approved.length, col: B.green },
    { lbl: "Rejected", val: rejected.length, col: B.red   },
  ];

  const LeaveItem = ({ l }) => (
    <div className="lv-item" style={{ padding: "14px 0" }}>
      <div className="lv-av">
        {(l.employeeName || "").split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2)}
      </div>
      <div style={{ flex: 1 }}>
        <div className="lv-nm">{l.employeeName}</div>
        <div className="lv-tp">{l.leaveType}</div>
        <div className="lv-dt">📅 {l.fromDate} → {l.toDate} · {l.numDays} working days</div>
        {l.reason && <div style={{ fontSize: 11, color: B.mutedL, marginTop: 2 }}>"{l.reason}"</div>}
      </div>
      {l.status === "PENDING" && (
        <div className="lv-btns">
          <button className="btn-ok" onClick={() => approve(l.id)}>✓ Approve</button>
          <button className="btn-no" onClick={() => reject(l.id)}>✕ Reject</button>
        </div>
      )}
      {l.status === "APPROVED"  && <span className="badge-ok">✓ Approved</span>}
      {(l.status === "REJECTED" || l.status === "CANCELLED") && (
        <span style={{ fontSize: 11, padding: "4px 10px", borderRadius: 20, background: B.redBg, color: B.red, fontWeight: 600 }}>
          ✕ {l.status}
        </span>
      )}
    </div>
  );

  return (
    <div className="ani">
      <div className="ou" />

      {/* Summary counts */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4,1fr)", gap: 12, marginBottom: 18 }}>
        {summaryCards.map(s => (
          <div key={s.lbl} style={{ background: B.white, border: `1px solid ${B.border}`, borderRadius: 12, padding: "16px 20px", boxShadow: B.shadow }}>
            <div style={{ fontSize: 11, color: B.muted, textTransform: "uppercase", letterSpacing: ".7px", fontWeight: 600 }}>{s.lbl}</div>
            <div style={{ fontSize: 28, fontWeight: 900, color: s.col, fontFamily: "Nunito,sans-serif", margin: "4px 0" }}>{s.val}</div>
          </div>
        ))}
      </div>

      {error   && <div style={{ background: "#fee2e2", border: "1px solid #fecaca", borderRadius: 9, padding: "10px 14px", fontSize: 13, color: "#dc2626", marginBottom: 16 }}>{error}</div>}
      {success && <div style={{ background: B.greenBg, border: "1px solid #bbf7d0",   borderRadius: 9, padding: "10px 14px", fontSize: 13, color: B.green,   marginBottom: 16 }}>{success}</div>}

      {/* Apply leave form */}
      <div className="card" style={{ marginBottom: 18 }}>
        <div className="card-hd">
          <span className="card-ttl">Apply for Leave</span>
          <button className="card-act" onClick={() => setShowForm(s => !s)}>
            {showForm ? "Hide ▲" : "New Request ＋"}
          </button>
        </div>

        {showForm && (
          <div style={{ marginTop: 16 }}>
            <div className="card-div" />
            <div className="fg-grid">
              <div className="fg">
                <label>Employee ID</label>
                <input
                  type="number" placeholder="e.g. 1"
                  value={form.employeeId}
                  onChange={e => setForm({ ...form, employeeId: e.target.value })}
                />
              </div>
              <div className="fg">
                <label>Leave Type</label>
                <select value={form.leaveTypeId} onChange={e => setForm({ ...form, leaveTypeId: e.target.value })}>
                  <option value="">Select type…</option>
                  {types.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                </select>
              </div>
              <div className="fg">
                <label>From Date</label>
                <input type="date" value={form.fromDate} onChange={e => setForm({ ...form, fromDate: e.target.value })} />
              </div>
              <div className="fg">
                <label>To Date</label>
                <input type="date" value={form.toDate} onChange={e => setForm({ ...form, toDate: e.target.value })} />
              </div>
              <div className="fg full">
                <label>Reason</label>
                <input placeholder="Reason for leave..." value={form.reason} onChange={e => setForm({ ...form, reason: e.target.value })} />
              </div>
            </div>
            <div style={{ display: "flex", gap: 10, marginTop: 16, justifyContent: "flex-end" }}>
              <button className="btn-ghost" onClick={() => setShowForm(false)}>Cancel</button>
              <button className="btn-orange" onClick={applyLeave} disabled={applying}>
                {applying ? "Submitting…" : "Submit Request"}
              </button>
            </div>
          </div>
        )}
      </div>

      {/* All requests */}
      <div className="card">
        <div className="card-hd">
          <span className="card-ttl">All Leave Applications</span>
        </div>
        <div className="card-div" />
        {leaves.length === 0
          ? <div className="empty">No leave requests at the moment 🎉</div>
          : leaves.map(l => <LeaveItem key={l.id} l={l} />)
        }
      </div>
    </div>
  );
}
