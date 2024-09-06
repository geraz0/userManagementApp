# User Management Application

This is a Spring Boot application for user management with roles `ADMIN` and `USER`. It allows users to register, update their own profile, and allows admins to manage all users (view, update, delete). The application uses in-memory authentication and an H2 database for data persistence.

## Features

- User Registration
- User Login
- Role-based access control (`ADMIN` and `USER`)
- Admin features:
    - View all users
    - Update any user
    - Delete any user
- Validation of user input with meaningful error messages
- Exception handling (e.g., `EntityNotFoundException` for missing users)

## Prerequisites

- JDK 17
- Maven 3.6+
- Postman (optional for testing API endpoints)

## Project Structure

- **Controller**: Manages the API endpoints.
- **Model**: Represents the `User` entity.
- **Repository**: Handles database operations for `User`.
- **Validation**: Custom password validation (optional).
- **Security Config**: Handles Spring Security setup for authentication and role-based access control.

## Security Roles
The application has two roles: ADMIN and USER. Depending on the role, users can access different endpoints.

USER: Can only view and update their own details.
ADMIN: Can view all users, update any user, and delete any user.

## Setup and Running the Application

### 1. Clone the repository
```bash
git clone https://github.com/your-username/user-management-app.git
cd user-management-app
```
### 2. Use Maven to build the project:
```bash
mvn clean install
```
### 3. Start the application using:
```bash
mvn spring-boot:run
```
### 4. Access the H2 Database Console
```bash
http://localhost:8080/h2-console
```
- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: (leave blank)
### 5. API Endpoints
#### Public Endpoints:
##### 1. Register a new user:
```bash
POST /api/users/register
```
#### User Endpoints (Requires Authentication):
##### 2. View own details:
```bash
GET /api/users/me
```
##### 3. Update own details:
```bash
PUT /api/users/me
```
#### Admin Endpoints (Requires ADMIN Role):
##### 4. View all users:
```bash
GET /api/users
```
##### 5. Update any user:
```bash
PUT /api/users/updateUser/{id}
```
##### 6. Delete any user:
```bash
DELETE /api/users/{id}
```

## Database Setup
The database connection is configured in src/main/resources/application.properties as follows:
```bash
# H2 database connection settings
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true  # Enable H2 web console
spring.jpa.hibernate.ddl-auto=update  # Auto-generate/update database schema
```
### User Entity Class
The User entity is mapped to the users table in the database:
```bash
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 10, message = "Username must be between 3 and 10 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Role cannot be blank")
    private String role;  // Stores user roles like "ADMIN" or "USER"
}
```
### Repository for Data Access
```bash
public interface UserRepository extends JpaRepository<User, Long> {
   Optional<User> findByUsername(String username);
}
```
### Database Initialization (Optional)
You can pre-load the database with some sample users using a data.sql file under src/main/resources/:
```bash
INSERT INTO users (username, password, email, role) VALUES ('admin_user', 'adminpass123', 'admin@example.com', 'ADMIN');
INSERT INTO users (username, password, email, role) VALUES ('user', 'password123', 'user@example.com', 'USER');
}
```
This will load default users when the application starts.

