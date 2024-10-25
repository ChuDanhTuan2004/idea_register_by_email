-- Bảng users để lưu thông tin người dùng
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    student_id VARCHAR(20) UNIQUE,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'INACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng registration_requests để lưu các yêu cầu đăng ký
CREATE TABLE registration_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(20),
    full_name VARCHAR(100),
    email VARCHAR(100),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- Bảng roles để lưu vai trò người dùng
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE
);

-- Bảng user_roles để mapping users và roles
CREATE TABLE user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Thêm dữ liệu mặc định cho roles
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_LIBRARIAN');
