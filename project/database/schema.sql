-- Performance Review System Database Schema
-- SQLite Database

-- Drop existing tables (for clean reset)
DROP TABLE IF EXISTS training_recommendations;
DROP TABLE IF EXISTS goals;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS users;

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    role TEXT DEFAULT 'EMPLOYEE' CHECK(role IN ('ADMIN', 'EMPLOYEE', 'MANAGER_EMPLOYEE', 'HIGHBOARD_MANAGER')),
    status TEXT DEFAULT 'PENDING' CHECK(status IN ('ACTIVE', 'PENDING', 'SUSPENDED')),
    tier_level INTEGER DEFAULT 1,
    manager_id INTEGER,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================
-- REVIEWS TABLE
-- ============================================
CREATE TABLE reviews (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    reviewer_id INTEGER,
    -- Basic categories (all employees)
    punctuality INTEGER DEFAULT 0 CHECK(punctuality BETWEEN 0 AND 10),
    teamwork INTEGER DEFAULT 0 CHECK(teamwork BETWEEN 0 AND 10),
    productivity INTEGER DEFAULT 0 CHECK(productivity BETWEEN 0 AND 10),
    -- Extended categories (managers only)
    leadership INTEGER DEFAULT 0 CHECK(leadership BETWEEN 0 AND 10),
    vision INTEGER DEFAULT 0 CHECK(vision BETWEEN 0 AND 10),
    managerial_skills INTEGER DEFAULT 0 CHECK(managerial_skills BETWEEN 0 AND 10),
    comments TEXT,
    status TEXT DEFAULT 'SUBMITTED',
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================
-- GOALS TABLE
-- ============================================
CREATE TABLE goals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    creator_id INTEGER,
    description TEXT NOT NULL,
    status TEXT DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'COMPLETED')),
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================
-- COURSES TABLE
-- ============================================
CREATE TABLE courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL,
    description TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TRAINING RECOMMENDATIONS TABLE
-- ============================================
CREATE TABLE training_recommendations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    recipient_id INTEGER NOT NULL,
    recommender_id INTEGER,
    course_name TEXT NOT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (recommender_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================
-- INDEXES for Performance
-- ============================================
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_manager ON users(manager_id);
CREATE INDEX idx_reviews_employee ON reviews(employee_id);
CREATE INDEX idx_reviews_reviewer ON reviews(reviewer_id);
CREATE INDEX idx_goals_employee ON goals(employee_id);
CREATE INDEX idx_training_recipient ON training_recommendations(recipient_id);

-- ============================================
-- DEFAULT ADMIN USER
-- ============================================
INSERT INTO users (username, password, name, role, status, tier_level) 
VALUES ('admin', 'admin', 'System Administrator', 'ADMIN', 'ACTIVE', 0);

-- ============================================
-- SAMPLE COURSES
-- ============================================
INSERT INTO courses (name) VALUES ('Leadership Fundamentals');
INSERT INTO courses (name) VALUES ('Time Management');
INSERT INTO courses (name) VALUES ('Team Communication');
INSERT INTO courses (name) VALUES ('Project Management');
INSERT INTO courses (name) VALUES ('Conflict Resolution');
