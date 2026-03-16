import { B } from "../data/constants";

const DEPARTMENTS = [
  { id: 1, name: "Engineering"  },
  { id: 2, name: "Product"      },
  { id: 3, name: "Design"       },
  { id: 4, name: "Analytics"    },
  { id: 5, name: "HR"           },
  { id: 6, name: "Finance"      },
  { id: 7, name: "Operations"   },
];

export default function AddEmployeeModal({ newE, setNewE, onAdd, onClose, loading }) {
  return (
    <div className="m-ov" onClick={e => e.target.className === "m-ov" && onClose()}>
      <div className="modal">
        <div className="modal-hd">
          <div className="modal-ttl">Add New Employee</div>
          <div className="modal-sub">Onboard a new team member to UniqueHire</div>
        </div>

        <div className="modal-body">
          <div className="fg-grid">
            <div className="fg">
              <label>First Name *</label>
              <input
                placeholder="e.g. Ananya"
                value={newE.firstName}
                onChange={e => setNewE({ ...newE, firstName: e.target.value })}
              />
            </div>

            <div className="fg">
              <label>Last Name *</label>
              <input
                placeholder="e.g. Singh"
                value={newE.lastName}
                onChange={e => setNewE({ ...newE, lastName: e.target.value })}
              />
            </div>

            <div className="fg">
              <label>Designation *</label>
              <input
                placeholder="e.g. Software Engineer"
                value={newE.designation}
                onChange={e => setNewE({ ...newE, designation: e.target.value })}
              />
            </div>

            <div className="fg">
              <label>Department *</label>
              <select
                value={newE.departmentId}
                onChange={e => setNewE({ ...newE, departmentId: parseInt(e.target.value) })}
              >
                {DEPARTMENTS.map(d => (
                  <option key={d.id} value={d.id}>{d.name}</option>
                ))}
              </select>
            </div>

            <div className="fg">
              <label>Email *</label>
              <input
                type="email"
                placeholder="name@uniquehire.co.in"
                value={newE.email}
                onChange={e => setNewE({ ...newE, email: e.target.value })}
              />
            </div>

            <div className="fg">
              <label>Phone</label>
              <input
                placeholder="+91 99999 00000"
                value={newE.phone}
                onChange={e => setNewE({ ...newE, phone: e.target.value })}
              />
            </div>

            <div className="fg">
              <label>Annual Salary (₹) *</label>
              <input
                type="number"
                placeholder="e.g. 800000"
                value={newE.annualSalary}
                onChange={e => setNewE({ ...newE, annualSalary: e.target.value })}
              />
            </div>

            <div className="fg">
              <label>Date of Joining *</label>
              <input
                type="date"
                value={newE.dateOfJoining}
                onChange={e => setNewE({ ...newE, dateOfJoining: e.target.value })}
              />
            </div>
          </div>

          <div className="modal-ft">
            <button className="btn-ghost" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button
              className="btn-orange"
              onClick={onAdd}
              disabled={loading}
              style={{ opacity: loading ? 0.7 : 1 }}
            >
              {loading ? "Adding…" : "＋ Add Employee"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
