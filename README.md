# ✅ TaskFlow — Full-Stack Task Management App

<div align="center">

![Angular](https://img.shields.io/badge/Angular-21-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?style=for-the-badge&logo=typescript&logoColor=white)

A modern, full-stack **task management web application** with an Angular 21 frontend and a Spring Boot 3 REST API backend. TaskFlow supports multi-user task tracking, priority and status filtering, task assignment, inline comments, a live activity feed, and an admin panel — all secured with stateless JWT authentication.

</div>

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Configuration](#-configuration)
- [API Reference](#-api-reference)
- [Frontend Pages & Routes](#-frontend-pages--routes)
- [Security](#-security)
- [Contributing](#-contributing)

---

## ✨ Features

### 🗂️ Task Management
- Create, view, edit, and delete tasks
- Per-task fields: **title**, **description**, **due date**, **status**, **priority**, **assignee**
- Status workflow: `TODO` → `IN_PROGRESS` → `DONE`
- Priority levels: `HIGH`, `MEDIUM`, `LOW`
- Filter tasks by status tab or priority
- Sort tasks by priority with a single toggle
- Overdue and due-today counters computed automatically

### 💬 Comments
- Add inline comments to any task
- View the full comment thread per task
- Delete your own comments

### 📊 Analytics Panel
- Task summary dashboard showing:
  - Total tasks, broken down by status and priority
  - Completion rate (%)
  - Tasks created this week
  - Overdue count and due-today count

### 📰 Activity Feed
- Real-time log of the last 20 user actions
- Tracked events: task created, status changed, priority changed, assigned, deleted, comment added
- One-click "Clear all" to reset the feed
- Relative timestamps ("2 minutes ago") via a custom Angular pipe

### 👤 User & Auth
- Register with full name, email, and password
- Login returns a JWT (24-hour expiry)
- Authenticated user context available throughout the app
- Guest guard prevents logged-in users from seeing login/register pages

### 🛡️ Admin Panel
- Admin account auto-provisioned at startup via configurable credentials
- Admin-only route guarded on both frontend and backend
- View all registered users (name, email, status, admin flag, join date)
- Activate or deactivate user accounts (admin accounts cannot be deactivated)

---

## 🛠 Tech Stack

### Backend
| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Validation | Jakarta Bean Validation |
| Utilities | Lombok |
| Build | Maven |

### Frontend
| Layer | Technology |
|-------|-----------|
| Framework | Angular 21 (standalone components) |
| Language | TypeScript 5.9 |
| HTTP | Angular HttpClient + JWT Interceptor |
| Routing | Angular Router with route guards |
| Styling | Component-scoped CSS |
| Testing | Vitest |
| Package Manager | npm 11 |

---

## 📁 Project Structure

```
TaskFlow/
├── taskflow-backend/               # Spring Boot REST API
│   └── src/main/java/com/example/taskflow/
│       ├── config/                 # SecurityConfig, AdminBootstrapConfig
│       ├── controller/             # REST controllers
│       ├── domain/                 # JPA entities & enums (Task, User, ActivityLog, etc.)
│       ├── dto/                    # Request/Response DTOs
│       ├── exception/              # GlobalExceptionHandler + custom exceptions
│       ├── repository/             # Spring Data JPA repositories
│       ├── security/               # JWT filter, JwtTokenService, UserPrincipal
│       └── service/                # Business logic (interface + impl)
│
└── taskflow-frontend/              # Angular 21 SPA
    └── src/app/
        ├── admin/                  # Admin user management panel
        ├── auth/                   # Login & Register components
        ├── guards/                 # AuthGuard, AdminGuard, GuestGuard
        ├── interceptors/           # JWT HTTP interceptor
        ├── models/                 # TypeScript interfaces (Task, User, Activity)
        ├── services/               # AuthService, TaskService
        ├── shared/
        │   ├── navbar/             # Shared navigation bar
        │   └── pipes/              # RelativeTimePipe
        └── tasks/
            ├── dashboard/          # Main task board + analytics panel
            ├── task-form/          # Create / edit task modal
            ├── task-comments/      # Inline comment thread
            └── activity-feed/      # Live activity log component
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Node.js 20+ & npm 11+
- MySQL 8.0+
- Angular CLI 21 (`npm install -g @angular/cli`)

---

### Backend Setup

**1. Create the database**

```sql
CREATE DATABASE taskflow;
```

**2. Clone and configure**

```bash
git clone https://github.com/your-username/taskflow.git
cd taskflow/taskflow-backend
```

Edit `src/main/resources/application.properties` with your MySQL credentials and a strong JWT secret (see [Configuration](#-configuration)).

**3. Build and run**

```bash
./mvnw clean spring-boot:run
```

The API starts at `http://localhost:8080`.

> On first run, the admin user is automatically created using `app.admin.email` and `app.admin.password` from your config.

---

### Frontend Setup

```bash
cd taskflow/taskflow-frontend
npm install
ng serve
```

The app opens at `http://localhost:4200`.

The frontend talks to `http://localhost:8080` by default (configured in `src/environments/environment.development.ts`). Update `apiBaseUrl` if your backend runs elsewhere.

---

## ⚙️ Configuration

`taskflow-backend/src/main/resources/application.properties`:

```properties
# DataSource (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/taskflow
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT — use a long random string (32+ chars)
app.jwt.secret=REPLACE_WITH_YOUR_SECRET_KEY_AT_LEAST_32_CHARACTERS
app.jwt.expiration-ms=86400000

# Admin bootstrap — auto-created on startup
app.admin.email=admin@taskflow.local
app.admin.password=YourStrongAdminPassword
```

> ⚠️ **Never commit real credentials.** Use environment variables or an external secrets manager in production.

---

## 📡 API Reference

All endpoints except `/api/auth/**` require `Authorization: Bearer <token>`.

### Auth — `/api/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/register` | Public | Register new user |
| POST | `/login` | Public | Login, returns JWT + user info |

### Tasks — `/api/tasks`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | User | List tasks (optional `?priority=HIGH\|MEDIUM\|LOW`) |
| POST | `/` | User | Create a new task |
| GET | `/{id}` | User | Get a single task |
| PUT | `/{id}` | User | Update a task |
| DELETE | `/{id}` | User | Delete a task |
| GET | `/summary` | User | Analytics summary (counts, rates) |

### Task Comments — `/api/tasks/{taskId}/comments`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | User | List comments for a task |
| POST | `/` | User | Add a comment |

### Comments — `/api/comments`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| DELETE | `/{id}` | User | Delete a comment |

### Activity Feed — `/api/activity`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | User | Get last 20 activity entries |
| DELETE | `/` | User | Clear all activity entries |

### Users — `/api/users`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | User | List users (for task assignment) |

### Admin — `/api/admin/users`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | Admin | List all users |
| PUT | `/{id}/activate` | Admin | Activate a user account |
| PUT | `/{id}/deactivate` | Admin | Deactivate a user account |
| DELETE | `/{id}` | Admin | Soft-delete (deactivate) a user |

---

### Sample Requests

**Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "yourpassword"
}
```
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "fullName": "Jane Doe",
  "email": "user@example.com",
  "admin": false
}
```

**Create Task**
```http
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Finish README",
  "description": "Write comprehensive project docs",
  "dueDate": "2026-06-01",
  "status": "TODO",
  "priority": "HIGH",
  "assignedToId": 2
}
```

---

## 🖥 Frontend Pages & Routes

| Route | Guard | Component | Description |
|-------|-------|-----------|-------------|
| `/login` | GuestGuard | LoginComponent | Login form |
| `/register` | GuestGuard | RegisterComponent | Registration form |
| `/dashboard` | AuthGuard | DashboardComponent | Main task board with analytics & activity feed |
| `/admin` | AdminGuard | AdminComponent | Admin user management panel |
| `/**` | — | NotFoundComponent | 404 page |

---

## 🔐 Security

- **Stateless JWT** — No server sessions. Every request is validated independently via `JwtAuthenticationFilter`.
- **BCrypt** password hashing via Spring Security's `BCryptPasswordEncoder`.
- **Role-based access** — Admin routes protected both at the API (`assertAdmin()` check) and the Angular frontend (`AdminGuard`) level.
- **JWT Interceptor** — Angular's `JwtInterceptor` automatically attaches the `Authorization: Bearer` header to all outbound API calls.
- **CORS** — Configured to allow `http://localhost:4200` during development. Update `corsConfigurationSource()` in `SecurityConfig` before deploying to production.
- **Admin bootstrap** — A single admin account is provisioned at startup; credentials are fully configurable via `application.properties`.

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'Add your feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

Please follow standard Java/Spring and Angular coding conventions. Ensure new API endpoints are secured appropriately and new Angular routes have the correct route guards applied.

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

<div align="center">
  Built with ❤️ using Angular 21 &amp; Spring Boot 3
</div>
