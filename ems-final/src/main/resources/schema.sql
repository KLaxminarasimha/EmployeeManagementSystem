CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

CREATE TABLE IF NOT EXISTS departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    manager_id  BIGINT,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS employees (
    id                BIGSERIAL PRIMARY KEY,
    employee_code     VARCHAR(20)   NOT NULL UNIQUE,
    first_name        VARCHAR(100)  NOT NULL,
    last_name         VARCHAR(100)  NOT NULL,
    email             VARCHAR(150)  NOT NULL UNIQUE,
    phone             VARCHAR(20),
    designation       VARCHAR(150),
    department_id     BIGINT        REFERENCES departments(id),
    employment_type   VARCHAR(20)   NOT NULL DEFAULT 'FULL_TIME',
    work_mode         VARCHAR(20)   NOT NULL DEFAULT 'OFFICE',
    status            VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    date_of_joining   DATE          NOT NULL,
    date_of_birth     DATE,
    address           TEXT,
    profile_photo     VARCHAR(500),
    annual_salary     NUMERIC(12,2) NOT NULL DEFAULT 0,
    leave_balance     INT           NOT NULL DEFAULT 18,
    manager_id        BIGINT        REFERENCES employees(id),
    created_at        TIMESTAMPTZ   DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   DEFAULT NOW()
);

ALTER TABLE departments ADD CONSTRAINT IF NOT EXISTS fk_dept_manager FOREIGN KEY (manager_id) REFERENCES employees(id);

CREATE INDEX IF NOT EXISTS idx_emp_dept      ON employees(department_id);
CREATE INDEX IF NOT EXISTS idx_emp_status    ON employees(status);
CREATE INDEX IF NOT EXISTS idx_emp_email     ON employees(email);
CREATE INDEX IF NOT EXISTS idx_emp_code      ON employees(employee_code);
CREATE INDEX IF NOT EXISTS idx_emp_name_trgm ON employees USING gin ((first_name || ' ' || last_name) gin_trgm_ops);

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(30)  NOT NULL DEFAULT 'EMPLOYEE',
    employee_id BIGINT       UNIQUE REFERENCES employees(id),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS attendance (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT      NOT NULL REFERENCES employees(id),
    attendance_date DATE        NOT NULL,
    check_in        TIMESTAMPTZ,
    check_out       TIMESTAMPTZ,
    work_hours      NUMERIC(4,2),
    status          VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    mark_method     VARCHAR(20) NOT NULL DEFAULT 'QR',
    wifi_ssid       VARCHAR(100),
    qr_token_used   VARCHAR(100),
    location        VARCHAR(20) DEFAULT 'OFFICE',
    notes           VARCHAR(500),
    marked_by       BIGINT      REFERENCES employees(id),
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (employee_id, attendance_date)
);

CREATE INDEX IF NOT EXISTS idx_att_emp_date ON attendance(employee_id, attendance_date);
CREATE INDEX IF NOT EXISTS idx_att_date     ON attendance(attendance_date);

CREATE TABLE IF NOT EXISTS qr_tokens (
    id           BIGSERIAL PRIMARY KEY,
    token_hash   VARCHAR(255) NOT NULL UNIQUE,
    generated_at TIMESTAMPTZ  DEFAULT NOW(),
    expires_at   TIMESTAMPTZ  NOT NULL,
    used_by      BIGINT       REFERENCES employees(id),
    used_at      TIMESTAMPTZ,
    is_used      BOOLEAN      DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS leave_types (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    annual_quota  INT          NOT NULL DEFAULT 0,
    is_paid       BOOLEAN      NOT NULL DEFAULT TRUE,
    carry_forward BOOLEAN      NOT NULL DEFAULT FALSE,
    description   VARCHAR(300),
    created_at    TIMESTAMPTZ  DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS leave_requests (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT      NOT NULL REFERENCES employees(id),
    leave_type_id   BIGINT      NOT NULL REFERENCES leave_types(id),
    from_date       DATE        NOT NULL,
    to_date         DATE        NOT NULL,
    num_days        INT         NOT NULL,
    reason          TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by     BIGINT      REFERENCES employees(id),
    reviewed_at     TIMESTAMPTZ,
    review_comment  VARCHAR(500),
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_leave_emp    ON leave_requests(employee_id);
CREATE INDEX IF NOT EXISTS idx_leave_status ON leave_requests(status);
CREATE INDEX IF NOT EXISTS idx_leave_dates  ON leave_requests(from_date, to_date);

CREATE TABLE IF NOT EXISTS payroll (
    id                BIGSERIAL PRIMARY KEY,
    employee_id       BIGINT        NOT NULL REFERENCES employees(id),
    payroll_month     DATE          NOT NULL,
    basic_salary      NUMERIC(12,2) NOT NULL,
    hra               NUMERIC(12,2) NOT NULL DEFAULT 0,
    special_allowance NUMERIC(12,2) NOT NULL DEFAULT 0,
    gross_salary      NUMERIC(12,2) NOT NULL,
    pf_deduction      NUMERIC(12,2) NOT NULL DEFAULT 0,
    professional_tax  NUMERIC(12,2) NOT NULL DEFAULT 200,
    income_tax        NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_deductions  NUMERIC(12,2) NOT NULL,
    net_salary        NUMERIC(12,2) NOT NULL,
    working_days      INT           NOT NULL DEFAULT 26,
    days_worked       INT           NOT NULL DEFAULT 26,
    leave_deduction   NUMERIC(12,2) DEFAULT 0,
    status            VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    processed_by      BIGINT        REFERENCES employees(id),
    processed_at      TIMESTAMPTZ,
    payment_date      DATE,
    payment_reference VARCHAR(100),
    created_at        TIMESTAMPTZ   DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   DEFAULT NOW(),
    UNIQUE (employee_id, payroll_month)
);

CREATE INDEX IF NOT EXISTS idx_payroll_emp   ON payroll(employee_id);
CREATE INDEX IF NOT EXISTS idx_payroll_month ON payroll(payroll_month);

CREATE TABLE IF NOT EXISTS performance_reviews (
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT       NOT NULL REFERENCES employees(id),
    reviewer_id   BIGINT       NOT NULL REFERENCES employees(id),
    review_period VARCHAR(20)  NOT NULL,
    review_date   DATE         NOT NULL,
    score         NUMERIC(4,1) NOT NULL CHECK (score BETWEEN 0 AND 100),
    rating        VARCHAR(30),
    strengths     TEXT,
    improvements  TEXT,
    goals_next    TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    created_at    TIMESTAMPTZ  DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_perf_emp    ON performance_reviews(employee_id);
CREATE INDEX IF NOT EXISTS idx_perf_period ON performance_reviews(review_period);

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   BIGINT       NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    changed_by  BIGINT       REFERENCES users(id),
    old_value   JSONB,
    new_value   JSONB,
    ip_address  VARCHAR(50),
    created_at  TIMESTAMPTZ  DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;

DO $$ BEGIN
  CREATE TRIGGER trg_emp_upd   BEFORE UPDATE ON employees           FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
  CREATE TRIGGER trg_att_upd   BEFORE UPDATE ON attendance          FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
  CREATE TRIGGER trg_leave_upd BEFORE UPDATE ON leave_requests      FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
  CREATE TRIGGER trg_pay_upd   BEFORE UPDATE ON payroll             FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
  CREATE TRIGGER trg_perf_upd  BEFORE UPDATE ON performance_reviews FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
  CREATE TRIGGER trg_dept_upd  BEFORE UPDATE ON departments         FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

INSERT INTO leave_types (name, annual_quota, is_paid, carry_forward) VALUES
  ('Annual Leave',18,TRUE,TRUE),('Sick Leave',7,TRUE,FALSE),('Casual Leave',7,TRUE,FALSE),
  ('Maternity Leave',90,TRUE,FALSE),('Paternity Leave',7,TRUE,FALSE),('Comp Off',5,TRUE,TRUE),('Loss of Pay',0,FALSE,FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO departments (name, description) VALUES
  ('Engineering','Software Development & Architecture'),('Product','Product Management & Strategy'),
  ('Design','UI/UX & Brand Design'),('Analytics','Data & Business Intelligence'),
  ('HR','Human Resources & Talent'),('Finance','Finance, Payroll & Compliance'),('Operations','Infrastructure & Operations')
ON CONFLICT (name) DO NOTHING;

-- Admin user — password: Admin@123
INSERT INTO users (username, password, role) VALUES
  ('admin','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y','ADMIN')
ON CONFLICT (username) DO NOTHING;
