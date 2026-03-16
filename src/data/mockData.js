// ── Mock employee data ─────────────────────────────────────
export const employees = [
  { id:1, name:"Arjun Sharma",  role:"Sr. Engineer",    dept:"Engineering", status:"office", av:"AS", salary:95000,  rating:4.8, leave:12, email:"arjun@uniquehire.co.in"  },
  { id:2, name:"Priya Nair",    role:"Product Manager", dept:"Product",     status:"wfh",    av:"PN", salary:110000, rating:4.5, leave:8,  email:"priya@uniquehire.co.in"  },
  { id:3, name:"Rahul Mehta",   role:"UI/UX Designer",  dept:"Design",      status:"leave",  av:"RM", salary:72000,  rating:4.2, leave:3,  email:"rahul@uniquehire.co.in"  },
  { id:4, name:"Sneha Reddy",   role:"Data Analyst",    dept:"Analytics",   status:"office", av:"SR", salary:85000,  rating:4.6, leave:15, email:"sneha@uniquehire.co.in"  },
  { id:5, name:"Karan Patel",   role:"DevOps Engineer", dept:"Engineering", status:"office", av:"KP", salary:92000,  rating:4.3, leave:10, email:"karan@uniquehire.co.in"  },
  { id:6, name:"Divya Iyer",    role:"HR Manager",      dept:"HR",          status:"wfh",    av:"DI", salary:78000,  rating:4.7, leave:6,  email:"divya@uniquehire.co.in"  },
  { id:7, name:"Vikram Nair",   role:"Finance Lead",    dept:"Finance",     status:"office", av:"VN", salary:102000, rating:4.4, leave:9,  email:"vikram@uniquehire.co.in" },
];

// ── Leave requests ──────────────────────────────────────────
export const leaveReqs = [
  { id:1, name:"Rahul Mehta",  type:"Sick Leave",   from:"Mar 15", to:"Mar 17", days:3, status:"pending"  },
  { id:2, name:"Sneha Reddy",  type:"Casual Leave", from:"Mar 20", to:"Mar 21", days:2, status:"approved" },
  { id:3, name:"Karan Patel",  type:"Annual Leave", from:"Apr 1",  to:"Apr 5",  days:5, status:"pending"  },
];

// ── Performance data ────────────────────────────────────────
export const perfData = [
  { name:"Arjun Sharma", q:[88,91,94,96] },
  { name:"Priya Nair",   q:[82,85,83,90] },
  { name:"Sneha Reddy",  q:[90,88,92,93] },
  { name:"Karan Patel",  q:[78,82,80,85] },
];

// ── Payroll monthly data ────────────────────────────────────
export const payMonths = [
  { m:"Oct",v:542000 },{ m:"Nov",v:558000 },{ m:"Dec",v:601000 },
  { m:"Jan",v:571000 },{ m:"Feb",v:583000 },{ m:"Mar",v:597000 },
];

// ── Weekly attendance data ──────────────────────────────────
export const attWeek = [
  { d:"Mon",o:22,w:6 },{ d:"Tue",o:25,w:5 },{ d:"Wed",o:20,w:8 },
  { d:"Thu",o:24,w:7 },{ d:"Fri",o:18,w:9 },{ d:"Sat",o:2,w:1  },{ d:"Sun",o:0,w:0  },
];

// ── Navigation items ────────────────────────────────────────
export const NAV = [
  { icon:"🏠", label:"Dashboard",   page:"dashboard"   },
  { icon:"👥", label:"Employees",   page:"employees"   },
  { icon:"📅", label:"Attendance",  page:"attendance"  },
  { icon:"💰", label:"Payroll",     page:"payroll"     },
  { icon:"⭐", label:"Performance", page:"performance" },
  { icon:"🌴", label:"Leave Mgmt",  page:"leave"       },
  { icon:"📊", label:"Reports",     page:"dashboard"   },
  { icon:"⚙️", label:"Settings",    page:"dashboard"   },
];
