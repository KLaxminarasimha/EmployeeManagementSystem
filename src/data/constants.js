// ── UniqueHire brand tokens ─────────────────────────────────
export const B = {
  orange:   "#E8671A",
  orangeL:  "#F4873D",
  orangeD:  "#C4520E",
  navy:     "#1B2F5E",
  navyL:    "#243671",
  navyD:    "#111E3C",
  white:    "#FFFFFF",
  offWhite: "#F5F7FA",
  light:    "#EEF1F7",
  muted:    "#7B87A0",
  mutedL:   "#A8B2C6",
  border:   "#E2E7F0",
  borderO:  "rgba(232,103,26,0.3)",
  green:    "#16a34a",
  greenBg:  "#dcfce7",
  blue:     "#1d4ed8",
  blueBg:   "#dbeafe",
  amber:    "#b45309",
  amberBg:  "#fef3c7",
  red:      "#dc2626",
  redBg:    "#fee2e2",
  shadow:   "0 1px 4px rgba(27,47,94,0.08)",
  shadowM:  "0 4px 16px rgba(27,47,94,0.1)",
};

// ── Department color map ────────────────────────────────────
export const deptColor = {
  Engineering: { bg:"#FFF0E8", text:B.orange,   dot:B.orange  },
  Product:     { bg:"#EBF0FF", text:B.navy,      dot:B.navyL   },
  Design:      { bg:"#F3EEFF", text:"#6d28d9",   dot:"#7c3aed" },
  Analytics:   { bg:"#ECFDF5", text:B.green,     dot:B.green   },
  HR:          { bg:"#FFF7ED", text:"#c2410c",   dot:"#ea580c" },
  Finance:     { bg:"#EFF6FF", text:B.blue,      dot:B.blue    },
};

// ── Status config map ───────────────────────────────────────
export const statusCfg = {
  office: { label:"In Office",       color:B.green,  bg:B.greenBg, dot:"#22c55e" },
  wfh:    { label:"Work from Home",  color:B.blue,   bg:B.blueBg,  dot:"#3b82f6" },
  leave:  { label:"On Leave",        color:B.amber,  bg:B.amberBg, dot:"#f59e0b" },
};

// ── Google Fonts import ─────────────────────────────────────
export const FONTS = `@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&family=Nunito:wght@600;700;800;900&display=swap');`;
