// ── Format currency in Indian style ───────────────────────
export const formatINR = (amount) => {
  if (amount >= 100000) return `₹${(amount / 100000).toFixed(2)}L`;
  if (amount >= 1000)   return `₹${(amount / 1000).toFixed(0)}K`;
  return `₹${amount}`;
};

// ── Get initials from a full name ──────────────────────────
export const getInitials = (name = "") =>
  name.split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2);

// ── Format date to readable string ────────────────────────
export const formatDate = (dateStr) => {
  if (!dateStr) return "—";
  return new Date(dateStr).toLocaleDateString("en-IN", {
    day:   "2-digit",
    month: "short",
    year:  "numeric",
  });
};

// ── Calculate payroll components ──────────────────────────
export const calcPayroll = (basicSalary) => {
  const hra   = Math.round(basicSalary * 0.2);
  const allow = Math.round(basicSalary * 0.1);
  const gross = basicSalary + hra + allow;
  const pf    = Math.round(basicSalary * 0.12);
  const pt    = 200;
  const tax   = Math.round(basicSalary * 0.083); // approx monthly
  const net   = gross - pf - pt - tax;
  return { hra, allow, gross, pf, pt, tax, net };
};

// ── Get working days between two dates ────────────────────
export const countWorkingDays = (from, to) => {
  let count = 0;
  const start = new Date(from);
  const end   = new Date(to);
  const d     = new Date(start);
  while (d <= end) {
    const dow = d.getDay();
    if (dow !== 0 && dow !== 6) count++;
    d.setDate(d.getDate() + 1);
  }
  return count;
};

// ── Truncate long strings ──────────────────────────────────
export const truncate = (str, maxLen = 30) =>
  str && str.length > maxLen ? str.slice(0, maxLen) + "…" : str;
