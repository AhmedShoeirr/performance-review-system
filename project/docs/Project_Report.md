# Performance Review System
## Project Report

---

## 1. Brief Description of the Project

The **Performance Review System** is a comprehensive Java-based desktop application designed to manage and streamline the employee performance evaluation process within an organization. The system implements a hierarchical structure with multiple user roles and provides features for 360-degree performance feedback (Sargeant et al., 2005).

The application is built using Java Swing for the graphical user interface and SQLite for database persistence (SQLite Consortium, 2023). It supports a three-tier organizational hierarchy where employees can view their own performance metrics, managers can evaluate their direct reports, and administrators can manage the entire system (Aguinis, 2019).

### Key Features:
- **Multi-role Authentication**: Support for Admin, Employee, Manager, and Highboard Manager roles with role-based access control
- **Performance Reviews**: 6-category scoring system (Punctuality, Teamwork, Productivity, Leadership, Vision, and Managerial Skills)
- **Goal Management**: CRUD operations for employee and team goals
- **Chain of Command**: Visual hierarchy display showing management structure
- **Training Recommendations**: Course suggestions from managers to employees
- **PDF Report Export**: Generate performance reports in exportable format

---

## 2. Objectives

### Primary Objectives:
1. **Automate Performance Review Process**: Replace manual paper-based performance evaluations with a digital system that ensures consistency and accuracy (Armstrong, 2020).

2. **Implement Role-Based Access Control**: Create a secure system where users can only access features appropriate to their organizational role.

3. **Enable 360° Feedback Collection**: Allow performance feedback from multiple sources including direct managers and higher-level supervisors (Bracken & Rose, 2011).

4. **Provide Real-Time Performance Tracking**: Enable employees to view their performance scores and track improvement over time.

5. **Facilitate Goal Setting and Tracking**: Support SMART goal management for improved employee development (Locke & Latham, 2002).

### Secondary Objectives:
- Create an intuitive, user-friendly interface
- Ensure data persistence using SQLite database
- Generate comprehensive performance reports
- Support organizational hierarchy visualization
- Enable training recommendations and skill development tracking

---

## 3. Project Design (UML Class Diagram)

The UML class diagram represents the object-oriented design of the system, showing all classes and their relationships.

### Key Design Patterns Used:
- **Singleton Pattern**: Applied to `DataStore` and `DatabaseManager` classes for centralized data management (Gamma et al., 1994)
- **Inheritance**: `User` abstract class extended by `Admin`, `Employee`, and `Manager` concrete classes
- **Composition**: `Review`, `Goal`, and `TrainingRecommendation` classes composed with `User` objects

### UML Class Diagram:

![UML Class Diagram](C:/Users/SOftLapTop/.gemini/antigravity/brain/378dfe4f-18f7-463f-97a9-223ab14207bd/uploaded_image_1_1765316489208.jpg)

### Main Classes:

| Class | Description | Key Attributes |
|-------|-------------|----------------|
| **User** (abstract) | Base class for all users | username, password, name, role, tierLevel |
| **Admin** | Administrator with full access | Inherits from User |
| **Employee** | Regular employee | Inherits from User |
| **Manager** | Manager with direct reports | Inherits from User |
| **Review** | Performance evaluation | employee, reviewer, scores (6 categories) |
| **Goal** | Employee objectives | employee, creator, description, status |
| **DataStore** | Singleton data manager | users, reviews, goals collections |
| **DatabaseManager** | Database operations | SQLite connection, CRUD methods |

### Class Relationships:
- **Inheritance**: Admin, Employee, Manager → User
- **Self-Association**: User manages other Users (hierarchy)
- **Composition**: Review, Goal → User

---

## 4. ERD (Entity Relationship Diagram)

The ERD shows the database schema with all tables and their relationships, implementing the relational model (Date, 2003).

![ERD Diagram](C:/Users/SOftLapTop/.gemini/antigravity/brain/378dfe4f-18f7-463f-97a9-223ab14207bd/uploaded_image_2_1765316489208.jpg)

### Database Tables:

| Table | Primary Key | Foreign Keys | Description |
|-------|-------------|--------------|-------------|
| **USERS** | id | manager_id → USERS(id) | All system users |
| **REVIEWS** | id | employee_id, reviewer_id → USERS(id) | Performance reviews |
| **GOALS** | id | employee_id, creator_id → USERS(id) | Employee goals |
| **TRAINING_RECOMMENDATIONS** | id | recipient_id, recommender_id → USERS(id) | Training suggestions |
| **COURSES** | id | - | Available courses |

### Key Relationships:
1. **USERS → USERS** (Self-referential): One-to-Many for organizational hierarchy (manager_id)
2. **USERS → REVIEWS**: One-to-Many (employee receives reviews, reviewer gives reviews)
3. **USERS → GOALS**: One-to-Many (employee has goals, creator creates goals)
4. **USERS → TRAINING_RECOMMENDATIONS**: One-to-Many (recipient receives, recommender recommends)

---

## 5. Screenshots of Project Execution

### 5.1 Login and Registration Screen

![Login and Registration](C:/Users/SOftLapTop/.gemini/antigravity/brain/378dfe4f-18f7-463f-97a9-223ab14207bd/uploaded_image_0_1765316489208.jpg)

**Features Shown:**
- User registration with password validation
- Password must contain: 8+ characters, uppercase, lowercase, number, and symbol
- Eye icon for password visibility toggle

---

### 5.2 Employee Dashboard - Performance Tab

![Employee Performance Dashboard](C:/Users/SOftLapTop/.gemini/antigravity/brain/378dfe4f-18f7-463f-97a9-223ab14207bd/uploaded_image_3_1765316489208.jpg)

**Features Shown:**
- Welcome message with user name and role
- Tabs: My Performance, My Reviews, Training, Chain of Command, My Goals
- Performance metrics display (Punctuality, Teamwork, Productivity)
- Overall score calculation
- Refresh and Export PDF buttons

---

### 5.3 Employee Dashboard - Chain of Command

![Chain of Command View](C:/Users/SOftLapTop/.gemini/antigravity/brain/378dfe4f-18f7-463f-97a9-223ab14207bd/uploaded_image_4_1765316489208.jpg)

**Features Shown:**
- Hierarchical display of management structure
- Level 1 and Level 2 managers shown
- Clear visualization of reporting relationships

---

## 6. References

Aguinis, H. (2019). *Performance Management* (4th ed.). Chicago Business Press.

Armstrong, M. (2020). *Armstrong's Handbook of Human Resource Management Practice* (15th ed.). Kogan Page.

Bracken, D. W., & Rose, D. S. (2011). When does 360-degree feedback create behavior change? And how would we know it when it does? *Journal of Business and Psychology*, 26(2), 183-192.

Date, C. J. (2003). *An Introduction to Database Systems* (8th ed.). Pearson Education.

Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley.

Locke, E. A., & Latham, G. P. (2002). Building a practically useful theory of goal setting and task motivation: A 35-year odyssey. *American Psychologist*, 57(9), 705-717.

Oracle Corporation. (2023). *Java SE Documentation*. Retrieved from https://docs.oracle.com/en/java/

Sargeant, J., Mann, K., Sinclair, D., Van der Vleuten, C., & Metsemakers, J. (2005). Understanding the influence of emotions and reflection upon multi-source feedback acceptance and use. *Advances in Health Sciences Education*, 10(4), 275-297.

SQLite Consortium. (2023). *SQLite Documentation*. Retrieved from https://www.sqlite.org/docs.html

---

*Report prepared for Software Engineering Project*  
*Performance Review System - Version 1.0*
