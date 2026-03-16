import { B } from "../data/constants";

export const CSS = `
*{box-sizing:border-box;margin:0;padding:0;}
body{font-family:'Poppins',sans-serif;background:${B.offWhite};color:${B.navy};min-height:100vh;}
.app{display:flex;min-height:100vh;}

/* ── Sidebar ── */
.sb{
  width:256px;min-height:100vh;position:fixed;top:0;left:0;bottom:0;
  background:${B.navy};
  display:flex;flex-direction:column;z-index:100;
  box-shadow:2px 0 12px rgba(27,47,94,0.15);
}
.sb-logo{
  padding:22px 20px 18px;
  border-bottom:1px solid rgba(255,255,255,0.08);
  display:flex;align-items:center;gap:12px;
}
.sb-tagline{font-size:10px;color:rgba(255,255,255,0.35);margin-top:2px;letter-spacing:.5px;font-weight:400;}
.sb-nav{flex:1;padding:16px 12px;display:flex;flex-direction:column;gap:1px;overflow-y:auto;}
.sb-sec{font-size:9px;color:rgba(255,255,255,0.25);letter-spacing:1.4px;text-transform:uppercase;padding:14px 10px 5px;font-weight:700;}
.nav-item{
  display:flex;align-items:center;gap:11px;padding:10px 12px;
  border-radius:10px;cursor:pointer;transition:all .15s;
  font-size:13px;color:rgba(255,255,255,0.5);font-weight:400;
}
.nav-item:hover{background:rgba(255,255,255,0.07);color:rgba(255,255,255,0.85);}
.nav-item.active{
  background:${B.orange};color:${B.white};font-weight:600;
  box-shadow:0 4px 12px rgba(232,103,26,0.4);
}
.nav-item.active .ni{opacity:1;}
.ni{font-size:16px;width:22px;text-align:center;flex-shrink:0;opacity:.6;}
.sb-bottom{padding:14px 12px;border-top:1px solid rgba(255,255,255,0.08);}
.sb-user{
  display:flex;align-items:center;gap:10px;padding:10px 12px;
  border-radius:10px;background:rgba(255,255,255,0.06);
  cursor:pointer;transition:background .15s;
}
.sb-user:hover{background:rgba(255,255,255,0.1);}
.sb-av{
  width:36px;height:36px;border-radius:10px;flex-shrink:0;
  background:linear-gradient(135deg,${B.orange},${B.orangeL});
  display:flex;align-items:center;justify-content:center;
  font-size:13px;font-weight:700;color:#fff;
}
.sb-nm{font-size:13px;font-weight:600;color:rgba(255,255,255,.85);}
.sb-rl{font-size:11px;color:rgba(255,255,255,.35);}
.sb-online{width:8px;height:8px;border-radius:50%;background:#22c55e;margin-left:auto;flex-shrink:0;box-shadow:0 0 0 2px rgba(34,197,94,0.2);}

/* ── Main ── */
.main{margin-left:256px;flex:1;background:${B.offWhite};min-height:100vh;}

/* ── Topbar ── */
.topbar{
  background:${B.white};border-bottom:1px solid ${B.border};
  padding:16px 36px;display:flex;align-items:center;justify-content:space-between;
  position:sticky;top:0;z-index:50;
  box-shadow:${B.shadow};
}
.page-title{font-family:'Nunito',sans-serif;font-size:22px;font-weight:900;color:${B.navy};letter-spacing:-.3px;}
.page-sub{font-size:12px;color:${B.muted};margin-top:2px;}
.tb-right{display:flex;align-items:center;gap:10px;}
.wifi-pill{
  display:flex;align-items:center;gap:6px;
  background:${B.greenBg};border:1px solid #bbf7d0;
  border-radius:20px;padding:6px 14px;font-size:12px;color:${B.green};font-weight:600;
}
.tb-btn{
  width:38px;height:38px;border-radius:10px;
  background:${B.offWhite};border:1px solid ${B.border};
  display:flex;align-items:center;justify-content:center;
  font-size:16px;cursor:pointer;transition:all .15s;
  box-shadow:${B.shadow};
}
.tb-btn:hover{border-color:${B.orange};background:${B.white};}

/* ── Page body ── */
.body{padding:28px 36px;}

/* ── Stat cards ── */
.stats-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:24px;}
.stat-card{
  background:${B.white};border:1px solid ${B.border};
  border-radius:14px;padding:20px 22px;position:relative;overflow:hidden;
  box-shadow:${B.shadow};transition:box-shadow .15s,transform .15s;cursor:default;
}
.stat-card:hover{box-shadow:${B.shadowM};transform:translateY(-2px);}
.stat-stripe{position:absolute;left:0;top:0;bottom:0;width:4px;border-radius:14px 0 0 14px;}
.stat-lbl{font-size:11px;color:${B.muted};text-transform:uppercase;letter-spacing:.8px;font-weight:600;padding-left:14px;}
.stat-val{font-family:'Nunito',sans-serif;font-size:36px;font-weight:900;color:${B.navy};margin:6px 0 2px;line-height:1;padding-left:14px;}
.stat-sub{font-size:12px;padding-left:14px;font-weight:500;}
.stat-ico{position:absolute;right:18px;top:50%;transform:translateY(-50%);font-size:32px;opacity:.08;}

/* ── Accent line ── */
.ou{width:36px;height:3px;background:${B.orange};border-radius:2px;margin:6px 0 20px;}

/* ── Grid layouts ── */
.g2{display:grid;grid-template-columns:1fr 1fr;gap:18px;margin-bottom:18px;}
.g31{display:grid;grid-template-columns:2fr 1fr;gap:18px;margin-bottom:18px;}

/* ── Cards ── */
.card{background:${B.white};border:1px solid ${B.border};border-radius:14px;padding:22px 24px;box-shadow:${B.shadow};}
.card-hd{display:flex;align-items:center;justify-content:space-between;margin-bottom:4px;}
.card-ttl{font-family:'Nunito',sans-serif;font-size:15px;font-weight:800;color:${B.navy};}
.card-act{
  font-size:11px;color:${B.orange};cursor:pointer;padding:5px 12px;
  border-radius:6px;border:1px solid ${B.borderO};
  font-weight:600;transition:background .15s;
}
.card-act:hover{background:#FFF0E8;}
.card-div{height:1px;background:${B.border};margin:14px 0;}

/* ── Tables ── */
.tbl{width:100%;border-collapse:collapse;}
.tbl th{font-size:10px;color:${B.muted};text-transform:uppercase;letter-spacing:.9px;padding:0 10px 12px;text-align:left;font-weight:700;border-bottom:1px solid ${B.border};}
.tbl-row{transition:background .1s;cursor:pointer;}
.tbl-row:hover{background:#FFF7F3;}
.tbl-row td{padding:12px 10px;font-size:13px;border-bottom:1px solid ${B.light};}
.tbl-row:last-child td{border-bottom:none;}
.emp-wrap{display:flex;align-items:center;gap:10px;}
.emp-av{width:36px;height:36px;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:700;color:#fff;flex-shrink:0;}
.emp-nm{font-weight:600;color:${B.navy};font-size:13px;}
.emp-rl{font-size:11px;color:${B.muted};margin-top:1px;}
.dept-badge{font-size:11px;padding:4px 10px;border-radius:20px;font-weight:600;}
.st-pill{display:inline-flex;align-items:center;gap:5px;font-size:11px;padding:4px 10px;border-radius:20px;font-weight:600;}
.st-dot-c{width:6px;height:6px;border-radius:50%;flex-shrink:0;}

/* ── Search bar ── */
.srch-bar{display:flex;gap:10px;margin-bottom:18px;}
.srch-in{
  flex:1;background:${B.white};border:1px solid ${B.border};
  border-radius:10px;padding:10px 16px;font-size:13px;color:${B.navy};
  outline:none;font-family:'Poppins',sans-serif;
  box-shadow:${B.shadow};transition:border-color .15s;
}
.srch-in:focus{border-color:${B.orange};}
.srch-in::placeholder{color:${B.mutedL};}
.filt-btn{
  background:${B.white};border:1px solid ${B.border};border-radius:10px;
  padding:9px 16px;font-size:12px;color:${B.muted};cursor:pointer;
  white-space:nowrap;font-family:'Poppins',sans-serif;font-weight:500;
  box-shadow:${B.shadow};transition:all .15s;
}
.filt-btn:hover{border-color:${B.orange};color:${B.orange};}

/* ── Buttons ── */
.btn-orange{
  background:${B.orange};color:#fff;border:none;border-radius:10px;
  padding:10px 22px;font-size:13px;cursor:pointer;
  font-family:'Poppins',sans-serif;font-weight:600;
  display:flex;align-items:center;gap:7px;
  box-shadow:0 4px 12px rgba(232,103,26,0.35);transition:all .15s;
}
.btn-orange:hover{background:${B.orangeD};box-shadow:0 4px 16px rgba(232,103,26,0.5);}
.btn-navy{
  background:${B.navy};color:#fff;border:none;border-radius:10px;
  padding:10px 22px;font-size:13px;cursor:pointer;
  font-family:'Poppins',sans-serif;font-weight:600;
  display:flex;align-items:center;gap:7px;
  box-shadow:0 4px 12px rgba(27,47,94,0.25);transition:all .15s;
}
.btn-navy:hover{background:${B.navyL};}

/* ── Leave items ── */
.lv-item{display:flex;align-items:center;gap:12px;padding:12px 0;border-bottom:1px solid ${B.light};}
.lv-item:last-child{border-bottom:none;}
.lv-av{width:38px;height:38px;border-radius:10px;background:${B.orange};display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:700;color:#fff;flex-shrink:0;}
.lv-nm{font-size:13px;font-weight:600;color:${B.navy};}
.lv-tp{font-size:12px;color:${B.muted};}
.lv-dt{font-size:11px;color:${B.mutedL};margin-top:1px;}
.lv-btns{display:flex;gap:6px;flex-shrink:0;}
.btn-ok{font-size:12px;padding:6px 12px;border-radius:7px;border:none;cursor:pointer;background:${B.greenBg};color:${B.green};font-weight:600;transition:all .15s;}
.btn-ok:hover{background:#bbf7d0;}
.btn-no{font-size:12px;padding:6px 12px;border-radius:7px;border:none;cursor:pointer;background:${B.redBg};color:${B.red};font-weight:600;transition:all .15s;}
.btn-no:hover{background:#fecaca;}
.badge-ok{font-size:11px;padding:4px 10px;border-radius:20px;background:${B.greenBg};color:${B.green};font-weight:600;border:1px solid #bbf7d0;}
.badge-pend{font-size:11px;padding:4px 10px;border-radius:20px;background:${B.amberBg};color:${B.amber};font-weight:600;border:1px solid #fde68a;}

/* ── Payroll bars ── */
.pay-wrap{display:flex;align-items:flex-end;gap:8px;height:72px;margin:12px 0 4px;}
.pay-bw{flex:1;display:flex;flex-direction:column;align-items:center;gap:4px;}
.pay-b{width:100%;border-radius:4px 4px 0 0;background:${B.orange};transition:height .3s;}
.pay-lbl{font-size:10px;color:${B.muted};font-weight:500;}

/* ── Performance rows ── */
.pf-row{display:flex;align-items:center;gap:12px;padding:9px 0;border-bottom:1px solid ${B.light};}
.pf-row:last-child{border-bottom:none;}
.pf-nm{font-size:12px;color:${B.navy};width:100px;flex-shrink:0;font-weight:500;}
.pf-bars{flex:1;display:flex;gap:3px;align-items:flex-end;height:28px;}
.pf-q{flex:1;border-radius:2px;}
.pf-sc{font-size:14px;font-weight:700;color:${B.orange};width:32px;text-align:right;}

/* ── Attendance bars ── */
.att-wrp{display:flex;align-items:flex-end;gap:8px;height:90px;}
.att-bw{flex:1;display:flex;flex-direction:column;align-items:center;gap:4px;}
.att-b{width:100%;border-radius:3px 3px 0 0;}
.att-dy{font-size:10px;color:${B.muted};font-weight:500;}

/* ── QR panel ── */
.qr-panel{display:flex;flex-direction:column;align-items:center;gap:12px;}
.qr-frame{
  width:124px;height:124px;border-radius:14px;
  border:2px solid ${B.orange};background:#FFF8F4;
  display:flex;align-items:center;justify-content:center;
  box-shadow:0 4px 12px rgba(232,103,26,0.12);
}
.qr-lbl{font-size:12px;color:${B.muted};text-align:center;line-height:1.6;font-weight:400;}
.wifi-tag{
  display:flex;align-items:center;gap:6px;
  background:${B.greenBg};border:1px solid #bbf7d0;
  border-radius:8px;padding:7px 14px;font-size:12px;color:${B.green};font-weight:600;
}
.wfh-note{font-size:11px;color:${B.mutedL};text-align:center;line-height:1.5;}

/* ── Tabs ── */
.tab-bar{display:flex;gap:4px;background:${B.white};border:1px solid ${B.border};border-radius:10px;padding:4px;margin-bottom:20px;width:fit-content;box-shadow:${B.shadow};}
.tab-btn{padding:7px 18px;border-radius:7px;font-size:12px;cursor:pointer;border:none;background:transparent;color:${B.muted};font-family:'Poppins',sans-serif;font-weight:500;transition:all .15s;}
.tab-btn.active{background:${B.orange};color:#fff;font-weight:600;box-shadow:0 3px 8px rgba(232,103,26,0.3);}

/* ── Modal ── */
.m-ov{position:fixed;inset:0;background:rgba(27,47,94,0.4);display:flex;align-items:center;justify-content:center;z-index:200;backdrop-filter:blur(6px);}
.modal{
  background:${B.white};border:1px solid ${B.border};border-radius:18px;
  padding:0;width:500px;max-width:96vw;overflow:hidden;
  box-shadow:0 20px 60px rgba(27,47,94,0.2);
}
.modal-hd{background:${B.navy};padding:24px 28px;}
.modal-ttl{font-family:'Nunito',sans-serif;font-size:20px;font-weight:900;color:#fff;}
.modal-sub{font-size:12px;color:rgba(255,255,255,.45);margin-top:3px;}
.modal-body{padding:24px 28px;}
.fg-grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}
.fg{display:flex;flex-direction:column;gap:5px;}
.fg.full{grid-column:span 2;}
.fg label{font-size:11px;color:${B.muted};font-weight:600;text-transform:uppercase;letter-spacing:.6px;}
.fg input,.fg select{
  background:${B.offWhite};border:1px solid ${B.border};border-radius:9px;
  padding:10px 14px;font-size:13px;color:${B.navy};
  outline:none;font-family:'Poppins',sans-serif;transition:border-color .15s;
}
.fg input:focus,.fg select:focus{border-color:${B.orange};background:${B.white};}
.fg select option{background:${B.white};color:${B.navy};}
.modal-ft{display:flex;gap:10px;justify-content:flex-end;margin-top:20px;}
.btn-ghost{background:transparent;color:${B.muted};border:1px solid ${B.border};border-radius:9px;padding:10px 22px;font-size:13px;cursor:pointer;font-family:'Poppins',sans-serif;font-weight:500;transition:all .15s;}
.btn-ghost:hover{border-color:${B.orange};color:${B.orange};}

/* ── Layout helpers ── */
.hdr-row{display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:24px;}
.sum-num{font-family:'Nunito',sans-serif;font-size:28px;font-weight:900;color:${B.navy};}
.sum-badge{font-size:12px;padding:3px 9px;border-radius:12px;background:${B.greenBg};color:${B.green};font-weight:600;margin-left:8px;vertical-align:middle;}
.sal-row{display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid ${B.light};}
.sal-row:last-child{border-bottom:none;}
.sal-k{font-size:13px;color:${B.muted};}
.sal-v{font-size:13px;font-weight:600;color:${B.navy};}
.sal-v.o{color:${B.orange};}.sal-v.g{color:${B.green};}.sal-v.r{color:${B.red};}
.prog-wrap{height:6px;background:${B.light};border-radius:3px;overflow:hidden;}
.prog-bar{height:100%;border-radius:3px;background:${B.orange};}
.empty{text-align:center;padding:36px;color:${B.muted};font-size:13px;}

/* ── Animation ── */
@keyframes fi{from{opacity:0;transform:translateY(8px);}to{opacity:1;transform:translateY(0);}}
.ani{animation:fi .22s ease forwards;}
`;
