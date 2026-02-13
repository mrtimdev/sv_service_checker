CREATE TABLE models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE fats_oils_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    km INT NOT NULL,
    min_km INT,
    max_km INT
);

CREATE TABLE drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    native_name VARCHAR(255),
    phone VARCHAR(255) NOT NULL UNIQUE,
    truck_id BIGINT
);

CREATE TABLE trucks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    license_plate VARCHAR(255) NOT NULL UNIQUE,
    model_name VARCHAR(255),
    group_name VARCHAR(255),
    year_of_manufacture VARCHAR(255),
    liter_quantity_of_fats DOUBLE NOT NULL,
    liter_quantity_of_oils DOUBLE NOT NULL,
    required_fat_oil BOOLEAN,
    required_inspection BOOLEAN,
    model_id BIGINT,
    year INT NOT NULL,
    fats_km_between DOUBLE NOT NULL,
    oils_km_between DOUBLE NOT NULL,
    km_for_oils_change DOUBLE NOT NULL,
    next_fats_range DOUBLE NOT NULL,
    next_oils_range DOUBLE NOT NULL,
    km_for_fats_shoot DOUBLE NOT NULL,
    expired_date DATE,
    size VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_km DOUBLE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    fats_oils_setting_id BIGINT
);

CREATE TABLE planning_repair_maintenance (
    planning_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE truck_fats_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    truck_id BIGINT NOT NULL,
    liter_quantity_of_fats DOUBLE NOT NULL,
    date DATE NOT NULL,
    current_km DOUBLE NOT NULL,
    distance_km DOUBLE NOT NULL,
    next_range DOUBLE NOT NULL,
    km_for_fats_shoot DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL,
    note VARCHAR(255),
    location_changed VARCHAR(255),
    file_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE truck_oils_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    truck_id BIGINT NOT NULL,
    liter_quantity_of_oils DOUBLE NOT NULL,
    date DATE NOT NULL,
    current_km DOUBLE NOT NULL,
    distance_km DOUBLE NOT NULL,
    next_range DOUBLE NOT NULL,
    km_for_oils_shoot DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL,
    note VARCHAR(255),
    location_changed VARCHAR(255),
    file_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
