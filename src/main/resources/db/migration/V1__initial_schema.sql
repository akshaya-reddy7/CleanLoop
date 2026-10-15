-- ========================================================
-- CLEANLOOP DATABASE SCHEMA MIGRATION: V1__initial_schema.sql
-- ========================================================

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NULL,
    role ENUM('CITIZEN', 'AUTHORITY', 'SERVICE_PROVIDER', 'ADMIN') NOT NULL DEFAULT 'CITIZEN',
    service_area VARCHAR(100) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_service_area (service_area),
    INDEX idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. WASTE CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS waste_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    is_recyclable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_categories_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. WASTE REPORTS TABLE
CREATE TABLE IF NOT EXISTS waste_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    issue_type VARCHAR(50) NOT NULL,
    user_category_id BIGINT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    address VARCHAR(255) NULL,
    locality VARCHAR(100) NULL,
    status ENUM('SUBMITTED', 'UNDER_REVIEW', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'REJECTED') NOT NULL DEFAULT 'SUBMITTED',
    assigned_to_id BIGINT NULL,
    resolution_notes TEXT NULL,
    resolved_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reports_category FOREIGN KEY (user_category_id) REFERENCES waste_categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_reports_assigned FOREIGN KEY (assigned_to_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_reports_reporter (reporter_id),
    INDEX idx_reports_status (status),
    INDEX idx_reports_category (user_category_id),
    INDEX idx_reports_locality (locality),
    INDEX idx_reports_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. REPORT IMAGES TABLE
CREATE TABLE IF NOT EXISTS report_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_images_report FOREIGN KEY (report_id) REFERENCES waste_reports (id) ON DELETE CASCADE,
    INDEX idx_images_report (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. WASTE CLASSIFICATIONS TABLE (AI Integration)
CREATE TABLE IF NOT EXISTS waste_classifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_image_id BIGINT NOT NULL UNIQUE,
    predicted_category_id BIGINT NULL,
    confidence_score DECIMAL(5, 4) NULL,
    model_version VARCHAR(50) NULL,
    classification_status ENUM('PENDING', 'SUCCESS', 'FAILED', 'UNAVAILABLE') NOT NULL DEFAULT 'PENDING',
    error_message VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_classifications_image FOREIGN KEY (report_image_id) REFERENCES report_images (id) ON DELETE CASCADE,
    CONSTRAINT fk_classifications_category FOREIGN KEY (predicted_category_id) REFERENCES waste_categories (id) ON DELETE SET NULL,
    INDEX idx_classifications_status (classification_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. REPORT STATUS HISTORY TABLE
CREATE TABLE IF NOT EXISTS report_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    from_status VARCHAR(50) NULL,
    to_status VARCHAR(50) NOT NULL,
    changed_by_id BIGINT NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_history_report FOREIGN KEY (report_id) REFERENCES waste_reports (id) ON DELETE CASCADE,
    CONSTRAINT fk_report_history_user FOREIGN KEY (changed_by_id) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_report_history_report (report_id),
    INDEX idx_report_history_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. PICKUP REQUESTS TABLE
CREATE TABLE IF NOT EXISTS pickup_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL,
    locality VARCHAR(100) NULL,
    preferred_date DATE NOT NULL,
    time_window VARCHAR(50) NOT NULL,
    image_path VARCHAR(500) NULL,
    is_special_handling BOOLEAN NOT NULL DEFAULT FALSE,
    special_handling_notes VARCHAR(255) NULL,
    status ENUM('REQUESTED', 'APPROVED', 'SCHEDULED', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REJECTED') NOT NULL DEFAULT 'REQUESTED',
    assigned_provider_id BIGINT NULL,
    completion_notes TEXT NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pickups_requester FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pickups_category FOREIGN KEY (category_id) REFERENCES waste_categories (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pickups_provider FOREIGN KEY (assigned_provider_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_pickups_requester (requester_id),
    INDEX idx_pickups_status (status),
    INDEX idx_pickups_provider (assigned_provider_id),
    INDEX idx_pickups_date (preferred_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. PICKUP STATUS HISTORY TABLE
CREATE TABLE IF NOT EXISTS pickup_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pickup_id BIGINT NOT NULL,
    from_status VARCHAR(50) NULL,
    to_status VARCHAR(50) NOT NULL,
    changed_by_id BIGINT NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pickup_history_pickup FOREIGN KEY (pickup_id) REFERENCES pickup_requests (id) ON DELETE CASCADE,
    CONSTRAINT fk_pickup_history_user FOREIGN KEY (changed_by_id) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_pickup_history_pickup (pickup_id),
    INDEX idx_pickup_history_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. COLLECTION SCHEDULES TABLE
CREATE TABLE IF NOT EXISTS collection_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    locality VARCHAR(100) NOT NULL,
    service_area VARCHAR(100) NOT NULL,
    collection_day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    category_id BIGINT NOT NULL,
    assigned_provider_id BIGINT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedules_category FOREIGN KEY (category_id) REFERENCES waste_categories (id) ON DELETE RESTRICT,
    CONSTRAINT fk_schedules_provider FOREIGN KEY (assigned_provider_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_schedules_locality (locality),
    INDEX idx_schedules_day (collection_day_of_week),
    INDEX idx_schedules_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. RECYCLING CENTERS TABLE
CREATE TABLE IF NOT EXISTS recycling_centers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    contact_phone VARCHAR(30) NULL,
    contact_email VARCHAR(100) NULL,
    operating_hours VARCHAR(100) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    website_url VARCHAR(255) NULL,
    accepted_materials VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_centers_location (latitude, longitude),
    INDEX idx_centers_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. ECO TIPS TABLE
CREATE TABLE IF NOT EXISTS eco_tips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(500) NULL,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tips_creator FOREIGN KEY (created_by_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_tips_category (category),
    INDEX idx_tips_published (is_published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. NOTIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference_id BIGINT NULL,
    reference_type VARCHAR(50) NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_notifications_user_read (user_id, is_read),
    INDEX idx_notifications_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. AUDIT LOGS TABLE
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT NULL,
    actor_email VARCHAR(150) NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(50) NULL,
    details TEXT NULL,
    ip_address VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_audit_actor (actor_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SEED INITIAL STANDARD WASTE CATEGORIES (Including 6 classes supported by TrashNet/MobileNetV2 + extended categories)
INSERT INTO waste_categories (code, name, description, is_recyclable) VALUES
('CARDBOARD', 'Cardboard', 'Corrugated boxes, paperboards, and packaging', TRUE),
('GLASS', 'Glass', 'Glass bottles, jars, and glassware', TRUE),
('METAL', 'Metal', 'Aluminum cans, tin containers, scrap metal', TRUE),
('PAPER', 'Paper', 'Newspapers, office paper, magazines', TRUE),
('PLASTIC', 'Plastic', 'Plastic bottles, containers, bags, and packaging', TRUE),
('TRASH', 'General Trash', 'Non-recyclable municipal solid waste', FALSE),
('ORGANIC', 'Organic / Food Waste', 'Food scraps, compostable waste, garden clippings', TRUE),
('E_WASTE', 'Electronic Waste', 'Computers, appliances, circuit boards, cables', TRUE),
('BULKY', 'Bulky Waste', 'Furniture, mattresses, large domestic items', FALSE),
('HAZARDOUS', 'Hazardous Waste', 'Chemicals, paints, fluorescent bulbs, hazardous domestic items', FALSE)
ON DUPLICATE KEY UPDATE name = VALUES(name);
