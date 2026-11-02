# Lost-and-Found Item Management System with Secure Ownership Verification

## 📌 Project Overview

The Lost-and-Found Item Management System is a web application developed using Java Spring Boot, MySQL, and Thymeleaf. It helps students report lost and found belongings, search reported items, and submit ownership claims.

The system provides secure ownership verification through confidential private clues and claimant evidence. Administrators can review claims, compare submitted evidence with private clues, and approve or reject claims to help ensure that items are returned to their rightful owners.

## ✨ Features

### 1. User Authentication and Security
- User registration and login.
- Password encryption using BCrypt.
- Role-based access control for students and administrators.
- Form validation and CSRF protection.
- Secure logout and session handling.

### 2. Lost and Found Item Reporting
- Report lost and found belongings.
- Enter item name, category, colour, location, date, and description.
- Automatically associate reports with the authenticated user.
- Add confidential identifying clues when reporting found items.

### 3. Item Catalog and Search
- Browse reported lost and found items.
- Search using keywords.
- Filter by category, report type, and item status.
- View item details and status badges.
- Keep private clues and reporter email addresses out of public listings.

### 4. Secure Ownership Verification
- Submit claims for found items.
- Provide an ownership explanation and supporting evidence.
- Prevent users from claiming items they reported finding.
- Prevent duplicate active claims.
- Prevent claims for lost or already-returned items.
- Track claim review status through the claimant dashboard.

### 5. Confidential Private Clues
- Store private clues separately from public item information.
- Restrict private clue access to authorized administrators.
- Keep confidential clues hidden from claimants.
- Support evidence-based ownership verification.

### 6. Administrator Dashboard
- View lost and found statistics.
- Monitor pending, approved, and rejected claims.
- Review claims using a dedicated verification interface.
- Compare private clues with claimant-submitted evidence.
- Approve or reject claims with decision notes.
- Mark approved items as returned.
- Dismiss conflicting rival claims after approval.

### 7. Sample Data Initialization
- Initialize a default administrator account and sample records when the database is fresh, according to the application's configuration.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| Spring Boot | Application framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database access |
| Hibernate | Object-relational mapping |
| MySQL | Database |
| Thymeleaf | Server-side HTML rendering |
| HTML and CSS | User interface |
| Bean Validation | Input validation |
| Maven | Dependency management and build |
| JUnit | Testing |

## 🏗️ Application Architecture

The application follows a layered architecture:

- **Controller Layer:** Handles HTTP requests and responses.
- **Service Layer:** Implements application logic and workflows.
- **Repository Layer:** Communicates with the database.
- **Entity Layer:** Represents users, items, claims, private clues, and evidence.
- **View Layer:** Displays pages using Thymeleaf templates.
- **Security Layer:** Manages login, permissions, and protected routes.

## 🗃️ Main Database Entities

- **User:** Stores user account information and roles.
- **Item:** Stores lost and found item details.
- **PrivateClue:** Stores confidential identifying information.
- **Claim:** Stores ownership claims and their review status.
- **ClaimEvidence:** Stores evidence submitted by claimants.

## ⚙️ Prerequisites

Install the following software:

- Java Development Kit (JDK)
- MySQL Server
- Maven Wrapper (included in the project)
- An IDE such as Antigravity or IntelliJ IDEA

## 🚀 How to Run the Project

### Step 1: Clone the Repository

```bash
git clone https://github.com/poojagajendran/Lost-and-Found-Management.git
```

### Step 2: Open the Project Folder

```powershell
cd Lost-and-Found-Management
```

If you already have the project locally, open its existing folder instead.

### Step 3: Configure the Database

Configure the MySQL connection in `src/main/resources/application.properties`.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lost_found_db?createDatabaseIfNotExist=true
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
```

Set the `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` environment variables to your own MySQL credentials.

Keep real credentials out of GitHub.

### Step 4: Run the Application

On Windows, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

### Step 5: Open the Application

Visit:

http://localhost:8080

The application requires the configured MySQL server to be running.

## 🧪 Run Tests

To execute the automated tests, run:

```powershell
.\mvnw.cmd test
```

The reported test results for this project were:

- **Total tests:** 13
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Build status:** SUCCESS

These results reflect the test run reported during development; run the tests again after making changes to verify the current code.

## 🔐 Security Considerations

- Passwords are encrypted using BCrypt.
- Role-based authorization protects administrator functionality.
- CSRF protection is enabled for supported forms.
- Private clues are separated from public item information.
- Ownership claims are associated with authenticated users.
- Database credentials should be supplied through environment variables.
- Default demo credentials should be changed before public deployment.

## 🔮 Future Enhancements

- Email notifications for claim updates.
- Item image uploads.
- Advanced search and filtering.
- Improved mobile interface.
- Deployment with a managed cloud database.
- Additional security and integration tests.

## 🎓 Project Purpose

This project demonstrates the development of a secure, database-driven web application using Java Spring Boot. It focuses on lost-and-found management, authentication, role-based access control, confidential information handling, and evidence-based ownership verification.

## 📄 License

Developed for educational and academic purposes.
