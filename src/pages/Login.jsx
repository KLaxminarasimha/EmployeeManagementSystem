import { useState } from "react";
import { B } from "../data/constants";
import { authApi, setToken } from "../utils/api";
import logo from "../assets/logo.png";

export default function Login({ onLogin }) {
  const [form, setForm] = useState({ username: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async () => {
    if (!form.username.trim() || !form.password.trim()) {
      setError("Please enter username and password.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await authApi.login(form);

      setToken(res.data.token);

      localStorage.setItem(
        "ems_user",
        JSON.stringify({
          userId: res.data.userId,
          employeeId: res.data.employeeId,
          fullName: res.data.fullName,
          role: res.data.role,
        })
      );

      onLogin(res.data);
    } catch (err) {
      setError(err.message || "Login failed. Check your credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: B.offWhite,
        fontFamily: "'Poppins', sans-serif",
      }}
    >
      <div
        style={{
          background: B.white,
          border: `1px solid ${B.border}`,
          borderRadius: 18,
          overflow: "hidden",
          width: 420,
          boxShadow: "0 20px 60px rgba(27,47,94,0.15)",
        }}
      >
        {/* Header */}
        <div style={{ background: B.navy, padding: "32px 36px 28px" }}>
          
          {/* Logo */}
          <div
            style={{
              background: B.white,
              borderRadius: 12,
              padding: "10px 18px",
              width: "fit-content",
              marginBottom: 18,
              boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
            }}
          >
            <img
              src={logo}
              alt="Logo"
              style={{
                width: 120,
                height: "auto",
                display: "block",
              }}
            />
          </div>

          <div
            style={{
              fontFamily: "'Nunito',sans-serif",
              fontSize: 22,
              fontWeight: 900,
              color: B.white,
            }}
          >
            Welcome back 👋
          </div>

          <div
            style={{
              fontSize: 13,
              color: "rgba(255,255,255,.45)",
              marginTop: 4,
            }}
          >
            Sign in to UniqueHire EMS
          </div>
        </div>

        {/* Form */}
        <div style={{ padding: "28px 36px" }}>
          {error && (
            <div
              style={{
                background: B.redBg,
                border: `1px solid #fecaca`,
                borderRadius: 9,
                padding: "10px 14px",
                fontSize: 13,
                color: B.red,
                marginBottom: 18,
              }}
            >
              {error}
            </div>
          )}

          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            
            {/* Username */}
            <div style={{ display: "flex", flexDirection: "column", gap: 5 }}>
              <label
                style={{
                  fontSize: 11,
                  color: B.muted,
                  fontWeight: 600,
                  textTransform: "uppercase",
                  letterSpacing: ".6px",
                }}
              >
                Username
              </label>

              <input
                type="text"
                placeholder="admin"
                value={form.username}
                onChange={(e) =>
                  setForm({ ...form, username: e.target.value })
                }
                onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
                style={{
                  background: B.offWhite,
                  border: `1px solid ${B.border}`,
                  borderRadius: 9,
                  padding: "11px 14px",
                  fontSize: 13,
                  color: B.navy,
                  outline: "none",
                  fontFamily: "'Poppins',sans-serif",
                }}
              />
            </div>

            {/* Password */}
            <div style={{ display: "flex", flexDirection: "column", gap: 5 }}>
              <label
                style={{
                  fontSize: 11,
                  color: B.muted,
                  fontWeight: 600,
                  textTransform: "uppercase",
                  letterSpacing: ".6px",
                }}
              >
                Password
              </label>

              <input
                type="password"
                placeholder="••••••••"
                value={form.password}
                onChange={(e) =>
                  setForm({ ...form, password: e.target.value })
                }
                onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
                style={{
                  background: B.offWhite,
                  border: `1px solid ${B.border}`,
                  borderRadius: 9,
                  padding: "11px 14px",
                  fontSize: 13,
                  color: B.navy,
                  outline: "none",
                  fontFamily: "'Poppins',sans-serif",
                }}
              />
            </div>

            {/* Button */}
            <button
              onClick={handleSubmit}
              disabled={loading}
              style={{
                background: loading ? B.mutedL : B.orange,
                color: "#fff",
                border: "none",
                borderRadius: 10,
                padding: "12px 0",
                fontSize: 14,
                fontWeight: 600,
                fontFamily: "'Poppins',sans-serif",
                cursor: loading ? "not-allowed" : "pointer",
                boxShadow: loading
                  ? "none"
                  : "0 4px 12px rgba(232,103,26,0.35)",
                transition: "all .15s",
                marginTop: 4,
              }}
            >
              {loading ? "Signing in..." : "Sign In →"}
            </button>
          </div>

          {/* Default credentials */}
          <div
            style={{
              marginTop: 20,
              padding: "12px 14px",
              background: B.offWhite,
              borderRadius: 9,
              fontSize: 12,
              color: B.muted,
              lineHeight: 1.7,
            }}
          >
            Default credentials:
            <br />
            Username: <strong style={{ color: B.navy }}>admin</strong> &nbsp;
            Password: <strong style={{ color: B.navy }}>Admin@123</strong>
          </div>
        </div>
      </div>
    </div>
  );
}