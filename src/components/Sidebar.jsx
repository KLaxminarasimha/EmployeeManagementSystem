import { NAV } from "../data/mockData";
import logo from "../assets/logo.png";

export default function Sidebar({ page, setPage }) {
  return (
    <aside className="sb">
      
      {/* Logo Section */}
      <div className="sb-logo">
        <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
          
          <img
            src={logo}
            alt="UniqueHire"
            style={{
              width: 150,
              height: "auto",
              background: "white",
              borderRadius: 10,
              padding: "6px 12px",
              boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
              objectFit: "contain"
            }}
          />

          <div className="sb-tagline">
            Employee Management System
          </div>

        </div>
      </div>

      {/* Navigation */}
      <nav className="sb-nav">
        
        <div className="sb-sec">Main</div>
        {NAV.slice(0, 2).map(n => (
          <div
            key={n.label}
            className={`nav-item${page === n.page ? " active" : ""}`}
            onClick={() => setPage(n.page)}
          >
            <span className="ni">{n.icon}</span>
            {n.label}
          </div>
        ))}

        <div className="sb-sec">Operations</div>
        {NAV.slice(2, 6).map(n => (
          <div
            key={n.label}
            className={`nav-item${page === n.page ? " active" : ""}`}
            onClick={() => setPage(n.page)}
          >
            <span className="ni">{n.icon}</span>
            {n.label}
          </div>
        ))}

        <div className="sb-sec">System</div>
        {NAV.slice(6).map(n => (
          <div
            key={n.label}
            className={`nav-item${page === n.page ? " active" : ""}`}
            onClick={() => setPage(n.page)}
          >
            <span className="ni">{n.icon}</span>
            {n.label}
          </div>
        ))}

      </nav>

      {/* Bottom User */}
      <div className="sb-bottom">
        <div className="sb-user">
          
          <div className="sb-av">AD</div>

          <div>
            <div className="sb-nm">Admin User</div>
            <div className="sb-rl">HR Manager</div>
          </div>

          <div className="sb-online" />

        </div>
      </div>

    </aside>
  );
}