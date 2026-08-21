# Backend to ReactJS Frontend Integration Guide

This guide contains the instructions, request/response models, and code patterns needed to integrate the ReactJS frontend with the Spring Boot Backend API.

---

## 1. General Configuration

- **API Base URL**: `http://localhost:3030`
- **Authentication**: JWT stateless authentication.
- **Authorization Header**: Client must include the JWT token in all secure requests:
  ```http
  Authorization: Bearer <your_jwt_token_here>
  ```
- **Content Type**: `application/json`

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
Authenticate credentials and get a JWT token.

- **URL**: `POST /api/auth/login`
- **Access**: Public
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
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYWlm...",
    "email": "saif@example.com"
  }
  ```

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

---

## 5. React Integration Patterns

### 5.1 Setting up Axios with JWT Interceptor

Here is the standard Axios instance implementation to handle authorization headers automatically in the React frontend:

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:3030/api',
});

// Request interceptor to attach JWT token
// Request interceptor to attach JWT token
// NOTE: Storing JWT tokens in localStorage makes them susceptible to XSS. In a production environment,
// it is highly recommended to use HttpOnly, Secure, and SameSite cookies for session tokens.
// If localStorage is required, ensure a robust Content Security Policy (CSP) is in place.
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle token expiration (401 Unauthorized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Skip redirects for public authentication endpoints (login, register)
      const isAuthRequest = error.config && (error.config.url.includes('/auth/login') || error.config.url.includes('/auth/register'));
      if (!isAuthRequest) {
        localStorage.removeItem('token');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
```
