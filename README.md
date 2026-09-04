# Contact Management System

A full-stack web application for managing personal and professional contacts, built as part of the 10Pearls Java Fullstack cohort (Cohort 9). The project covers the complete cycle of a real application: user authentication, a relational data layer, a REST API, a React front end, automated tests, and static code analysis.

## What the application does

Users can create an account, log in, and manage their own list of contacts. Each contact can hold multiple labeled emails and phone numbers (for example, "work" and "personal"), along with a title, company, address, and notes. Contacts can be searched, filtered, paginated, marked as favorites, edited, and deleted. Users can also export their contacts to a CSV file and import contacts from one, and can update their profile or change their password from within the app.

## Technology stack

**Backend**
- Java 17
- Spring Boot 4.1 (Web, Security, Data JPA, Validation)
- Hibernate as the JPA provider
- SQL Server as the database
- JJWT for JWT-based authentication
- Slf4j with Logback for logging
- JUnit 5 and Mockito for testing
- Apache Commons CSV for import/export

**Frontend**
- React 19 with Vite
- React Router for navigation
- Axios for API calls
- Plain CSS with component-scoped styling

**Tooling**
- Maven for backend builds
- SonarQube for static code analysis, configured to scan both the Java backend and the React frontend
- Git, with feature branches per unit of work

## Architecture

The backend follows a fairly standard layered structure: controllers handle HTTP requests, services hold business logic, repositories talk to the database through Spring Data JPA, and DTOs keep the API surface separate from the persistence model. A JWT filter sits in front of protected routes and reads the access token from an HttpOnly cookie rather than from local storage, which keeps the token out of reach of any injected client-side script. A global exception handler translates known error cases (like a duplicate email during registration, or a contact that doesn't exist) into consistent, readable API responses instead of raw stack traces.

The frontend is a single-page app with three main screens — login/registration, the contact dashboard, and the user profile — talking to the backend purely through REST calls.

## Key features

### Authentication
- Registration with email/password, with server-side validation and duplicate-email checks.
- Login that issues a JWT stored in an HttpOnly, SameSite cookie rather than exposed to JavaScript.
- Logout that clears the session cookie.
- Change password from the profile screen.
- An HTTPS-enforcement check on login/logout in production, so session cookies are never issued over an unencrypted connection outside of local development.

### Contact management
- Paginated listing of contacts, so the UI stays responsive even with a large contact list.
- Search and filter by name.
- Create, update, and delete contacts through modal forms, each with its own confirmation step for destructive actions.
- Support for multiple labeled emails and phone numbers per contact, stored as a proper relational structure rather than a single flat field.
- Mark contacts as favorites.

### Import and export
- Export the current contact list to a CSV file, written with a UTF-8 byte-order mark so it opens correctly in Excel.
- Import contacts from a CSV file, with validation on the uploaded data before it's persisted.

### Interface
- Light and dark theme, toggled from the navbar and remembered across the session.

### Logging and error handling
- Application events, authentication attempts, and errors are logged through Slf4j/Logback rather than printed to the console.
- A global exception handler returns structured, user-readable error messages and keeps internal exception details out of API responses.

### Testing and code quality
- Unit and integration tests across controllers, services, and utility classes (JUnit 5 and Mockito), covering authentication, contact operations, security configuration, and CSV handling.
- SonarQube is wired up at the repository root to analyze both the Java and JavaScript code together, with test coverage reported through JaCoCo.

## Application screens

**Login and Registration**
A login form and a registration form, with redirection to the contact dashboard once authentication succeeds.

| Login | Registration |
|---|---|
| <img width="1366" height="768" alt="01-login" src="https://github.com/user-attachments/assets/bb08908f-b3be-4f7c-9ab0-ce7385b31bc6" /> | <img width="1366" height="768" alt="02-register" src="https://github.com/user-attachments/assets/2e8731b7-15a4-4d56-9cd1-cbdea6727e7e" /> |


**Contact Dashboard**
The main working screen: a paginated, searchable list of contacts, with buttons to create, edit, and delete a contact. Creating or editing opens a modal with a form; saving persists the change and closes the modal, and cancelling discards it. Deleting a contact asks for confirmation before it happens.

![Empty contact dashboard]<img width="1366" height="768" alt="03-dashboard-empty" src="https://github.com/user-attachments/assets/03271353-edeb-45cd-a903-a2db77147ce8" />

*Dashboard on first login, before any contacts are added.*

![Create contact modal]<img width="1366" height="768" alt="04-create-contact-modal" src="https://github.com/user-attachments/assets/31c79c70-7b35-4876-beda-0a366c51284e" />

*Creating a new contact, including support for a title, company, and multiple labeled emails/phone numbers.*

**Import and Export**
Contacts can be bulk-loaded from a CSV file through the Import button, and the whole list can be downloaded the same way through Export.
<img width="1366" height="721" alt="Screenshot 2026-09-01 003244" src="https://github.com/user-attachments/assets/8754760e-1554-4756-9787-d720579a42d0" />


*Importing a batch of 100 contacts from a CSV file.*

![Dashboard with imported contacts and pagination with limit of 10 contacts on each page]<img width="1366" height="721" alt="Dashboard with imported contacts and pagination with limit of 10 contacts on each page" src="https://github.com/user-attachments/assets/1e794765-489b-46c2-a699-9fdffc7a8475" />

*The dashboard immediately after import, now holding 101 contacts.*

**Theme**
The interface supports both dark and light themes, switched from the navbar.

![Light theme]
<img width="1366" height="768" alt="07-light-theme" src="https://github.com/user-attachments/assets/b0da25bf-2abe-413d-a1ee-4b00528202a7" />

*The same dashboard in light mode.*

**User Profile**
Shows the logged-in user's details, with a change-password form and a logout action. The change-password flow runs in its own modal, separate from the rest of the profile screen.

| Profile Details | Change Password |
|---|---|
| <img width="1366" height="768" alt="Profile details" src="https://github.com/user-attachments/assets/5af52e22-bc01-4602-96b9-998b2c0fac0a" /> | <img width="1366" height="768" alt="Change password" src="https://github.com/user-attachments/assets/2d57e1af-d6bd-44bb-8d37-06b955552a58" /> |

## Repository layout

```
backend/contact-management-backend/   Spring Boot application (Java)
  src/main/java/.../controller/       REST controllers (Auth, User, Contact, Health)
  src/main/java/.../service/          Business logic
  src/main/java/.../repository/       Spring Data JPA repositories
  src/main/java/.../entity/           JPA entities (User, Contact)
  src/main/java/.../dto/              Request/response objects
  src/main/java/.../exception/        Custom exceptions and the global handler
  src/main/java/.../filter/           JWT authentication filter
  src/main/java/.../config/           Security configuration
  src/test/java/                      Unit and integration tests

frontend/                             React application (Vite)
  src/pages/                          Login, Register, Dashboard
  src/components/                     Reusable UI pieces (contact modal, navbar, profile modal)
  src/services/                       API client layer
  src/utils/                          Frontend helpers

sonar-project.properties              SonarQube configuration for the whole repo
```

## Running the project locally

The backend expects a running SQL Server instance and reads its connection details, JWT secret, and active Spring profile from environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`). For local development, the `local-development` profile should be active — this relaxes the HTTPS requirement that's otherwise enforced on the login and logout endpoints, since a local setup normally runs over plain HTTP.

```bash
cd backend/contact-management-backend
export DB_URL="jdbc:sqlserver://localhost:1433;databaseName=contact_management;encrypt=true;trustServerCertificate=true"
export DB_USERNAME="sa"
export DB_PASSWORD="your-db-password"
export JWT_SECRET="a-long-random-secret-used-to-sign-jwts"
export SPRING_PROFILES_ACTIVE=local-development
mvn spring-boot:run
```

The frontend is a standard Vite project:

```bash
cd frontend
npm install
npm run dev
```

By default the backend serves the API on port 3030, and the frontend dev server proxies requests to it.

## Notes on the development process

This project was built incrementally, with each feature developed on its own branch and reviewed before merging — including a round of automated code review that surfaced and fixed issues around accessibility, CSRF exemptions on logout, safe local storage handling, and consistent CSS naming. That review history is part of the commit log and reflects an intentional effort to treat code quality as part of the deliverable, not an afterthought.
