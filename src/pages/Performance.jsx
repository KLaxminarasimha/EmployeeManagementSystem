import { useState, useEffect } from "react";
import { B } from "../data/constants";
import { performanceApi } from "../utils/api";

const CURRENT_PERIOD = `Q1-${new Date().getFullYear()}`;

const RATING_COLORS = {
  EXCELLENT:        { color: B.green,  bg: B.greenBg  },
  GOOD:             { color: B.blue,   bg: B.blueBg   },
  AVERAGE:          { color: B.amber,  bg: B.amberBg  },
  NEEDS_IMPROVEMENT:{ color: B.red,    bg: B.redBg    },
};

export default function Performance() {
  const [reviews,  setReviews]  = useState([]);
  const [trend,    setTrend]    = useState([]);
  const [top,      setTop]      = useState([]);
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState("");

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [revRes, trendRes, topRes] = await Promise.all([
          performanceApi.getByPeriod(CURRENT_PERIOD),
          performanceApi.getTrend(),
          performanceApi.getTopPerformers(CURRENT_PERIOD),
        ]);
        setReviews(Array.isArray(revRes.data)   ? revRes.data   : []);
        setTrend(Array.isArray(trendRes.data)   ? trendRes.data : []);
        setTop(Array.isArray(topRes.data)       ? topRes.data   : []);
      } catch (e) {
        setError("Failed to load performance data: " + e.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return <div className="ani"><div className="empty">Loading performance data...</div></div>;

  return (
    <div className="ani">
      <div className="ou" />

      {error && (
        <div style={{ background: "#fee2e2", border: "1px solid #fecaca", borderRadius: 9, padding: "10px 14px", fontSize: 13, color: "#dc2626", marginBottom: 16 }}>
          {error}
        </div>
      )}

      {/* Trend chart */}
      {trend.length > 0 && (
        <div className="card" style={{ marginBottom: 18 }}>
          <div className="card-hd">
            <span className="card-ttl">Average Score Trend</span>
          </div>
          <div className="card-div" />
          <div style={{ display: "flex", alignItems: "flex-end", gap: 16, height: 80 }}>
            {trend.map((t, i) => {
              const h = `${(parseFloat(t.avgScore) / 100) * 70}px`;
              return (
                <div key={t.period} style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 4 }}>
                  <span style={{ fontSize: 11, color: B.muted, fontWeight: 500 }}>{t.avgScore}</span>
                  <div style={{
                    width: "100%", height: h, borderRadius: "4px 4px 0 0",
                    background: i === trend.length - 1 ? B.orange : B.navy,
                    opacity: i === trend.length - 1 ? 1 : 0.4,
                  }} />
                  <span style={{ fontSize: 10, color: B.mutedL }}>{t.period}</span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Top performers */}
      {top.length > 0 && (
        <div className="card" style={{ marginBottom: 18 }}>
          <div className="card-hd">
            <span className="card-ttl">🏆 Top Performers — {CURRENT_PERIOD}</span>
          </div>
          <div className="card-div" />
          {top.map((p, i) => {
            const rc  = RATING_COLORS[p.rating] || RATING_COLORS["GOOD"];
            const av  = (p.employeeName || "").split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2);
            return (
              <div key={p.id} style={{ display: "flex", alignItems: "center", gap: 12, padding: "10px 0", borderBottom: `1px solid ${B.light}` }}>
                <div style={{ width: 24, fontSize: 16, textAlign: "center", flexShrink: 0 }}>
                  {i === 0 ? "🥇" : i === 1 ? "🥈" : i === 2 ? "🥉" : `${i + 1}.`}
                </div>
                <div style={{ width: 36, height: 36, borderRadius: 10, background: B.orange, display: "flex", alignItems: "center", justifyContent: "center", color: "#fff", fontSize: 12, fontWeight: 700, flexShrink: 0 }}>{av}</div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 13, fontWeight: 600, color: B.navy }}>{p.employeeName}</div>
                  <div style={{ fontSize: 11, color: B.muted }}>{p.reviewPeriod}</div>
                </div>
                <span style={{ fontSize: 22, fontWeight: 900, color: B.orange, fontFamily: "Nunito,sans-serif" }}>{p.score}</span>
                <span style={{ fontSize: 11, padding: "3px 9px", borderRadius: 20, background: rc.bg, color: rc.color, fontWeight: 600 }}>
                  {p.rating?.replace("_", " ")}
                </span>
              </div>
            );
          })}
        </div>
      )}

      {/* All reviews grid */}
      {reviews.length > 0 ? (
        <div className="g2">
          {reviews.map(p => {
            const rc = RATING_COLORS[p.rating] || RATING_COLORS["GOOD"];
            const av = (p.employeeName || "").split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2);
            return (
              <div key={p.id} className="card">
                <div className="card-hd">
                  <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                    <div style={{ width: 36, height: 36, borderRadius: 10, background: B.orange, display: "flex", alignItems: "center", justifyContent: "center", color: "#fff", fontSize: 12, fontWeight: 700 }}>{av}</div>
                    <span className="card-ttl">{p.employeeName}</span>
                  </div>
                  <span style={{ fontSize: 26, fontWeight: 900, color: B.orange, fontFamily: "Nunito,sans-serif" }}>
                    {p.score}<span style={{ fontSize: 13, color: B.muted, fontWeight: 400 }}>/100</span>
                  </span>
                </div>
                <div className="card-div" />
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 10 }}>
                  <span style={{ fontSize: 12, padding: "3px 9px", borderRadius: 20, background: rc.bg, color: rc.color, fontWeight: 600 }}>
                    {p.rating?.replace("_", " ")}
                  </span>
                  <span style={{ fontSize: 12, color: B.muted }}>{p.reviewPeriod} · {p.status}</span>
                </div>
                {p.strengths && (
                  <div style={{ fontSize: 12, color: B.muted, marginBottom: 6 }}>
                    <strong style={{ color: B.navy }}>Strengths:</strong> {p.strengths}
                  </div>
                )}
                {p.improvements && (
                  <div style={{ fontSize: 12, color: B.muted }}>
                    <strong style={{ color: B.navy }}>Improve:</strong> {p.improvements}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      ) : (
        <div className="empty" style={{ padding: 48 }}>
          No performance reviews for {CURRENT_PERIOD} yet.
        </div>
      )}
    </div>
  );
}
