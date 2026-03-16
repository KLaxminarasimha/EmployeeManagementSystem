import { useState, useEffect } from "react";
import { B, deptColor, statusCfg } from "../data/constants";
import { attendanceApi } from "../utils/api";
import QRCode from "../components/QRCode";

const TODAY = new Date().toISOString().split("T")[0];
const MONDAY = (() => {
  const d = new Date();
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  return new Date(d.setDate(diff)).toISOString().split("T")[0];
})();

export default function Attendance({ empList, user }) {
  const [attTab,      setAttTab]      = useState("today");
  const [todayLog,    setTodayLog]    = useState([]);
  const [todayStats,  setTodayStats]  = useState(null);
  const [weeklyData,  setWeeklyData]  = useState([]);
  const [loading,     setLoading]     = useState(false);
  const [error,       setError]       = useState("");

  // ── Load attendance data ─────────────────────────────────
  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [logRes, statsRes, weekRes] = await Promise.all([
          attendanceApi.getToday(),
          attendanceApi.getTodayStats(),
          attendanceApi.getWeekly(MONDAY),
        ]);
        setTodayLog(Array.isArray(logRes.data)   ? logRes.data   : []);
        setTodayStats(statsRes.data || null);
        setWeeklyData(Array.isArray(weekRes.data) ? weekRes.data  : []);
      } catch (e) {
        setError("Failed to load attendance: " + e.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const maxO = weeklyData.length ? Math.max(...weeklyData.map(d => d.officeCount || 0), 1) : 1;
  const maxW = weeklyData.length ? Math.max(...weeklyData.map(d => d.wfhCount    || 0), 1) : 1;
  const maxBar = Math.max(maxO, maxW);

  return (
    <div className="ani">
      <div className="ou" />

      {error && (
        <div style={{ background: "#fee2e2", border: "1px solid #fecaca", borderRadius: 9, padding: "10px 14px", fontSize: 13, color: "#dc2626", marginBottom: 16 }}>
          {error}
        </div>
      )}

      {/* Tabs */}
      <div className="tab-bar">
        {["today", "week", "month"].map(t => (
          <button
            key={t}
            className={`tab-btn${attTab === t ? " active" : ""}`}
            onClick={() => setAttTab(t)}
          >
            {t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {/* Today stat pills */}
      {todayStats && (
        <div style={{ display: "flex", gap: 12, marginBottom: 18 }}>
          {[
            { label: "In Office", val: todayStats.inOffice, col: B.green,  bg: B.greenBg  },
            { label: "WFH",       val: todayStats.wfh,      col: B.blue,   bg: B.blueBg   },
            { label: "Absent",    val: todayStats.absent,   col: "#6b7280", bg: "#f3f4f6" },
            { label: "On Leave",  val: todayStats.onLeave,  col: B.amber,  bg: B.amberBg  },
          ].map(s => (
            <div key={s.label} style={{
              background: s.bg, border: `1px solid ${s.col}33`,
              borderRadius: 10, padding: "8px 16px",
              fontSize: 12, color: s.col, fontWeight: 600,
            }}>
              {s.val ?? "—"} {s.label}
            </div>
          ))}
        </div>
      )}

      <div className="g2">
        {/* Weekly chart */}
        <div className="card">
          <div className="card-hd">
            <span className="card-ttl">Weekly Attendance</span>
          </div>
          <div style={{ display: "flex", gap: 14, margin: "8px 0 14px" }}>
            {[{ c: B.orange, l: "In Office" }, { c: B.navy, l: "Work from Home" }].map(x => (
              <span key={x.l} style={{ display: "flex", alignItems: "center", gap: 5, fontSize: 12, color: B.muted, fontWeight: 500 }}>
                <span style={{ width: 10, height: 10, borderRadius: 2, background: x.c, display: "inline-block" }} />
                {x.l}
              </span>
            ))}
          </div>
          <div className="att-wrp">
            {weeklyData.length > 0
              ? weeklyData.map(d => (
                  <div key={d.dayName || d.date} className="att-bw">
                    <div className="att-b" style={{
                      height: `${((d.officeCount || 0) / maxBar) * 72}px`,
                      background: B.orange, borderRadius: "4px 4px 0 0",
                    }} />
                    <div className="att-b" style={{
                      height: `${((d.wfhCount || 0) / maxBar) * 72}px`,
                      background: B.navy, opacity: 0.5,
                      borderRadius: "4px 4px 0 0", marginTop: 2,
                    }} />
                    <span className="att-dy">
                      {d.dayName || new Date(d.date).toLocaleDateString("en", { weekday: "short" })}
                    </span>
                  </div>
                ))
              : <div className="empty" style={{ width: "100%", padding: 16 }}>No data this week</div>
            }
          </div>
        </div>

        {/* QR Scanner */}
        <div className="card">
          <div className="card-hd">
            <span className="card-ttl">QR Scanner</span>
          </div>
          <div className="card-div" />
          <div className="qr-panel">
            <div className="qr-frame"><QRCode /></div>
            <div className="qr-lbl">
              Scan at office entrance gate<br />
              Token refreshes every 30 seconds
            </div>
            <div className="wifi-tag">📶 Corp-WiFi-5G · Connected</div>
            <div className="wfh-note">
              WFH employees are automatically<br />
              marked via company WiFi detection
            </div>
          </div>
        </div>
      </div>

      {/* Today's log from API */}
      <div className="card">
        <div className="card-hd">
          <span className="card-ttl">Today's Attendance Log</span>
        </div>
        <div className="card-div" />
        {loading && <div className="empty">Loading...</div>}
        {!loading && (
          <table className="tbl">
            <thead>
              <tr>
                <th>Employee</th>
                <th>Check In</th>
                <th>Check Out</th>
                <th>Hours</th>
                <th>Method</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {todayLog.map(a => {
                const dept = empList.find(e => e.id === a.employeeId)?.department || "Engineering";
                const dc   = deptColor[dept] || { dot: B.orange };
                const loc  = a.location?.toLowerCase();
                const sc   = loc === "wfh" ? statusCfg["wfh"] : statusCfg["office"];
                const av   = (a.employeeName || "")
                  .split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2);
                const ci   = a.checkIn  ? new Date(a.checkIn).toLocaleTimeString("en-IN",  { hour: "2-digit", minute: "2-digit" }) : "—";
                const co   = a.checkOut ? new Date(a.checkOut).toLocaleTimeString("en-IN", { hour: "2-digit", minute: "2-digit" }) : "—";
                const hrs  = a.workHours ? `${parseFloat(a.workHours).toFixed(1)}h` : "—";
                return (
                  <tr key={a.id} className="tbl-row">
                    <td>
                      <div className="emp-wrap">
                        <div className="emp-av" style={{ background: dc.dot, width: 30, height: 30, borderRadius: 8, fontSize: 11 }}>{av}</div>
                        <span className="emp-nm">{a.employeeName}</span>
                      </div>
                    </td>
                    <td style={{ fontSize: 13, color: B.navy, fontWeight: 500 }}>{ci}</td>
                    <td style={{ fontSize: 13, color: B.muted }}>{co}</td>
                    <td style={{ fontSize: 13, color: B.navy }}>{hrs}</td>
                    <td>
                      {a.markMethod === "QR" && (
                        <span style={{ fontSize: 11, background: B.offWhite, border: `1px solid ${B.border}`, borderRadius: 6, padding: "3px 9px", color: B.navy, fontWeight: 500 }}>🔲 QR Scan</span>
                      )}
                      {(a.markMethod === "WIFI_AUTO" || a.markMethod === "WFH_AUTO") && (
                        <span style={{ fontSize: 11, background: B.blueBg, border: "1px solid #bfdbfe", borderRadius: 6, padding: "3px 9px", color: B.blue, fontWeight: 500 }}>📶 WiFi Auto</span>
                      )}
                      {a.markMethod === "MANUAL" && (
                        <span style={{ fontSize: 11, background: B.offWhite, border: `1px solid ${B.border}`, borderRadius: 6, padding: "3px 9px", color: B.muted, fontWeight: 500 }}>✍️ Manual</span>
                      )}
                    </td>
                    <td>
                      <span className="st-pill" style={{ background: sc?.bg || B.greenBg, color: sc?.color || B.green }}>
                        <span className="st-dot-c" style={{ background: sc?.dot || "#22c55e" }} />
                        {a.location || "OFFICE"}
                      </span>
                    </td>
                  </tr>
                );
              })}
              {todayLog.length === 0 && !loading && (
                <tr><td colSpan={6} className="empty">No attendance records for today</td></tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
