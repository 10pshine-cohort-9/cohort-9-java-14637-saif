# Backend to ReactJS Frontend Integration Guide

This guide contains the instructions, request/response models, and code patterns needed to integrate the ReactJS frontend with the Spring Boot Backend API.

---

## 1. General Configuration

- **API Base URL**: `http://localhost:3030/api`
- **Authentication**: JWT issued as an **HttpOnly, Secure, SameSite=Lax cookie** (`ACCESS_TOKEN`) — the token is never exposed to JavaScript or included in response bodies. The browser sends it automatically on every request to the API origin; clients must call requests with `credentials: 'include'` (fetch) or `withCredentials: true` (axios).
- **CSRF Protection**: Because auth is cookie-based, all state-changing requests (`POST`/`PUT`/`DELETE`, except `/api/auth/login` and `/api/auth/register`) require a CSRF token. The server issues a readable `XSRF-TOKEN` cookie on every response; the client must echo its value back in an `X-XSRF-TOKEN` request header. Axios does this automatically when configured with `withXSRFToken: true`.
- **Content Type**: `application/json` for all JSON endpoints. The CSV import/export endpoints below use different content types (`multipart/form-data` and `text/csv` respectively).

---

All error responses from the backend follow this unified JSON structure (handled by the global exception handler):

```json
{
  "timestamp": "2026-08-21T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "errors": {
    "firstName": "First name is required"
  }
}
```

---

## 3. Authentication & User Profile Endpoints

### 3.1 User Registration (Signup)
Register a new user using either email or phone number.

- **URL**: `POST /api/auth/register`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "firstName": "Saif",
    "lastName": "Ul Hassan",
    "email": "saif@example.com",
    "password": "SecurePassword123",
    "phoneNumber": "+1234567890"
  }
  ```
- **Response** (`201 Created`):
  ```json
  {
    "id": 1,
    "firstName": "Saif",
    "lastName": "Ul Hassan",
    "email": "saif@example.com",
    "phoneNumber": "+1234567890"
  }
  ```

### 3.2 User Login (Sign-In)
Authenticate credentials. On success, the server sets the `ACCESS_TOKEN` cookie (HttpOnly/Secure/SameSite=Lax) — the token itself is **not** included in the response body.

- **URL**: `POST /api/auth/login`
- **Access**: Public (exempt from CSRF)
- **Request Body**:
  ```json
  {
    "email": "saif@example.com",
    "password": "SecurePassword123"
  }
  ```
- **Response** (`200 OK`):
  ```json
  {
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "firstName": "Saif",
      "lastName": "Ul Hassan",
      "email": "saif@example.com",
      "phoneNumber": "+1234567890"
    }
  }
  ```

### 3.2.1 Logout
Clears the `ACCESS_TOKEN` cookie server-side.

- **URL**: `POST /api/auth/logout`
- **Access**: Public
- **Response** (`200 OK`): No response body.

### 3.3 Get Current User Profile
Retrieve the details of the logged-in user.

- **URL**: `GET /api/users/profile`
- **Access**: Secure
- **Response** (`200 OK`):
  ```json
  {
    "id": 1,
    "firstName": "Saif",
    "lastName": "Ul Hassan",
    "email": "saif@example.com",
    "phoneNumber": "+1234567890"
  }
  ```

### 3.4 Update User Profile
Update the logged-in user's contact details.

- **URL**: `PUT /api/users/profile`
- **Access**: Secure
- **Request Body**:
  ```json
  {
    "firstName": "Saif Updated",
    "lastName": "Ul Hassan",
    "phoneNumber": "+0987654321"
  }
  ```
- **Response** (`200 OK`):
  ```json
  {
    "id": 1,
    "firstName": "Saif Updated",
    "lastName": "Ul Hassan",
    "email": "saif@example.com",
    "phoneNumber": "+0987654321"
  }
  ```

### 3.5 Change Password
Changes the user's password. **Note**: This will invalidate all previously issued JWT tokens, forcing a re-login.

- **URL**: `POST /api/users/change-password`
- **Access**: Secure
- **Request Body**:
  ```json
  {
    "oldPassword": "SecurePassword123",
    "newPassword": "NewSecurePassword456"
  }
  ```
- **Response** (`200 OK`): No response body.

---

## 4. Contact Management Endpoints

Contacts are scoped to the authenticated user. A user can only view, modify, or delete their own contacts.

### 4.1 Create a Contact
Create a new contact. You can supply a single default `email`/`phoneNumber` or a dictionary of labeled `emails`/`phoneNumbers`.

- **URL**: `POST /api/contacts`
- **Access**: Secure
- **Request Body**:
  ```json
  {
    "firstName": "John",
    "lastName": "Doe",
    "title": "Mr.",
    "company": "Tech Corp",
    "address": "123 Main Street",
    "notes": "Met at conference",
    "favorite": true,
    "emails": {
      "work": "john.doe@work.com",
      "personal": "johndoe@gmail.com"
    },
    "phoneNumbers": {
      "work": "+111222333",
      "home": "+444555666"
    }
  }
  ```
- **Response** (`201 Created`):
  ```json
  {
    "id": 101,
    "firstName": "John",
    "lastName": "Doe",
    "title": "Mr.",
    "email": null,
    "phoneNumber": null,
    "company": "Tech Corp",
    "address": "123 Main Street",
    "notes": "Met at conference",
    "favorite": true,
    "userId": 1,
    "emails": {
      "work": "john.doe@work.com",
      "personal": "johndoe@gmail.com"
    },
    "phoneNumbers": {
      "work": "+111222333",
      "home": "+444555666"
    }
  }
  ```

### 4.2 Get Paginated Contacts List
Retrieve a list of the user's contacts. Pagination size is capped at a maximum of **10 contacts per page** to optimize performance.

- **URL**: `GET /api/contacts`
- **Access**: Secure
- **Query Parameters**:
  - `page` (optional, default: `0`): Page index (0-based).
  - `size` (optional, default: `10`, max: `10`): Page size.
  - `sortBy` (optional, default: `firstName`): Field to sort by.
  - `direction` (optional, default: `asc`): Sort direction (`asc` or `desc`).
- **Response** (`200 OK`):
  ```json
  {
    "content": [
      {
        "id": 101,
        "firstName": "John",
        "lastName": "Doe",
        "title": "Mr.",
        "email": null,
        "phoneNumber": null,
        "company": "Tech Corp",
        "address": "123 Main Street",
        "notes": "Met at conference",
        "favorite": true,
        "userId": 1,
        "emails": {
          "work": "john.doe@work.com",
          "personal": "johndoe@gmail.com"
        },
        "phoneNumbers": {
          "work": "+111222333",
          "home": "+444555666"
        }
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    "size": 10,
    "number": 0,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "numberOfElements": 1,
    "first": true,
    "empty": false
  }
  ```

### 4.3 Get Contact by ID
Retrieve details of a single contact.

- **URL**: `GET /api/contacts/{id}`
- **Access**: Secure
- **Response** (`200 OK`):
  ```json
  {
    "id": 101,
    "firstName": "John",
    "lastName": "Doe",
    "title": "Mr.",
    "email": null,
    "phoneNumber": null,
    "company": "Tech Corp",
    "address": "123 Main Street",
    "notes": "Met at conference",
    "favorite": true,
    "userId": 1,
    "emails": {
      "work": "john.doe@work.com",
      "personal": "johndoe@gmail.com"
    },
    "phoneNumbers": {
      "work": "+111222333",
      "home": "+444555666"
    }
  }
  ```

### 4.4 Update a Contact
Update contact fields and details.

- **URL**: `PUT /api/contacts/{id}`
- **Access**: Secure
- **Request Body**:
  ```json
  {
    "firstName": "John Updated",
    "lastName": "Doe",
    "title": "Dr.",
    "company": "Tech Corp Ltd",
    "address": "456 Oak Ave",
    "notes": "Updated notes",
    "favorite": false,
    "emails": {
      "work": "john.doe@work.com",
      "personal": "john.personal@outlook.com"
    },
    "phoneNumbers": {
      "work": "+999888777",
      "home": "+444555666"
    }
  }
  ```
- **Response** (`200 OK`): Updated contact JSON object.

### 4.5 Delete a Contact
Delete a contact.

- **URL**: `DELETE /api/contacts/{id}`
- **Access**: Secure
- **Response** (`204 No Content`): No body.

### 4.6 Search Contacts
Search contacts by first name or last name using keyword queries. Capped at a maximum of **10 contacts per page**.

- **URL**: `GET /api/contacts/search`
- **Access**: Secure
- **Query Parameters**:
  - `keyword` (required): Search keyword matching first name or last name.
  - `page` (optional, default: `0`): Page index.
  - `size` (optional, default: `10`, max: `10`): Page size limit.
  - `sortBy` (optional, default: `firstName`): Field to sort.
  - `direction` (optional, default: `asc`): Sort direction.
- **Response** (`200 OK`): Paginated contacts result.

### 4.7 Import Contacts from CSV
Import contacts from a CSV file. The CSV must use the UTF-8 charset and include a header row matching the export format (`firstName,lastName,title,email,phoneNumber,company,address,notes,favorite,emails,phoneNumbers`).

- **URL**: `POST /api/contacts/import`
- **Access**: Secure
- **Content Type**: `multipart/form-data`
- **Request Body**: A `file` form field containing the `.csv` file.
- **Response** (`200 OK`):
  ```json
  {
    "importedCount": 5
  }
  ```

### 4.8 Export Contacts to CSV
Export all of the current user's contacts as a CSV file.

- **URL**: `GET /api/contacts/export`
- **Access**: Secure
- **Response** (`200 OK`):
  - **Content-Type**: `text/csv`
  - **Content-Disposition**: `attachment; filename="contacts.csv"`
  - **Body**: Raw CSV content, UTF-8 encoded.

---

## 5. React Integration Patterns

### 5.1 Setting up Axios for Cookie-Based Auth

The access token is an HttpOnly cookie set by the server — the frontend never reads or stores it. The Axios instance only needs `withCredentials` (to send the cookie) and `withXSRFToken` (to echo the CSRF cookie back as a header on state-changing requests):

```javascript
import axios from 'axios';

export const EMAIL_KEY = 'email';
export const AUTH_FLAG_KEY = 'isAuthenticated'; // UI-only hint; the server enforces auth via the cookie

export const logout = async () => {
  try {
    await api.post('/auth/logout'); // clears the ACCESS_TOKEN cookie server-side
  } catch (e) {
    // Best-effort: proceed with local cleanup even if the request fails.
  }
  localStorage.removeItem(EMAIL_KEY);
  localStorage.removeItem(AUTH_FLAG_KEY);
  window.location.href = '/login';
};

const api = axios.create({
  baseURL: 'http://localhost:3030/api',
  withCredentials: true, // send the ACCESS_TOKEN cookie
  withXSRFToken: true,   // echo the XSRF-TOKEN cookie as the X-XSRF-TOKEN header
});

// Response interceptor to handle an expired/missing session (401 Unauthorized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Skip redirects for public authentication endpoints (login, register)
      const url = error.config && error.config.url ? String(error.config.url).toLowerCase() : '';
      const isAuthRequest = url && (url.includes('/auth/login') || url.includes('/auth/register'));
      if (!isAuthRequest) {
        localStorage.removeItem(EMAIL_KEY);
        localStorage.removeItem(AUTH_FLAG_KEY);
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
```

On successful login, set the UI-only auth flag (used by route guards) alongside the display email:

```javascript
const response = await api.post('/auth/login', { email, password });
localStorage.setItem(EMAIL_KEY, response.data.user?.email || email);
localStorage.setItem(AUTH_FLAG_KEY, 'true');
```
