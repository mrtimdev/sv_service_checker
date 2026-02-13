-- Migration: add Planning Repair & Maintenance tables (new feature)
-- Safe to run on old schema (tables do not exist).

CREATE TABLE IF NOT EXISTS planning_repair_maintenance (
    planning_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    truck_id BIGINT NOT NULL,
    plan_title VARCHAR(255),
    plan_task_text TEXT,
    actual_task_text TEXT,
    note_text TEXT,
    planned_start_date DATE,
    planned_end_date DATE,
    planned_duration_days INT,
    actual_start_date DATETIME,
    actual_end_date DATETIME,
    interval_type VARCHAR(20) NOT NULL,
    interval_value INT,
    next_planned_date DATE,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_prm_truck FOREIGN KEY (truck_id) REFERENCES trucks(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE INDEX idx_prm_truck_id ON planning_repair_maintenance (truck_id);
CREATE INDEX idx_prm_status ON planning_repair_maintenance (status);
CREATE INDEX idx_prm_planned_start ON planning_repair_maintenance (planned_start_date);

CREATE TABLE IF NOT EXISTS planning_repair_maintenance_attachments (
    attachment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    planning_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_path VARCHAR(255),
    file_type VARCHAR(100),
    uploaded_at DATETIME,
    CONSTRAINT fk_prm_attachment FOREIGN KEY (planning_id) REFERENCES planning_repair_maintenance(planning_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE INDEX idx_prm_attachment_planning_id ON planning_repair_maintenance_attachments (planning_id);
