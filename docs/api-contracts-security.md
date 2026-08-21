# API Contracts & Security

## 1. API Contracts

### Authentication APIs

| Method | Endpoint | Description | Access |
| --- | --- | --- | --- |
| POST | `/api/login` | Login using username and password | Public |
| POST | `/api/register` | Create a new account | Public |
| POST | `/api/forgot-password` | Request password reset | Public |
| POST | `/api/reset-password` | Reset password using OTP/token | Public |

### Activities API

```http
GET /api/activities
```

Example response:

```json
[
  {
    "id": 1,
    "typeId": 2,
    "subjectId": 5,
    "startTime": "2026-08-20T09:00:00",
    "endTime": "2026-08-20T11:00:00",
    "duration": 120
  }
]
```

---

# 2. Authentication

Authentication answers:

> **Who are you?**

The main authentication operations are:

- Login
- Register
- Forget Password
- Reset Password

### Login

The client sends:

```text
username
password
```

The server validates the credentials.

If they are correct, the server generates an authentication token, commonly a JWT.

```text
User
 ↓
Username + Password
 ↓
POST /api/login
 ↓
Server validates credentials
 ↓
Generate JWT
 ↓
Return JWT
 ↓
Client stores token
```

### Register

```http
POST /api/register
```

Example:

```json
{
  "username": "ahmed",
  "password": "123456",
  "role": "Employee"
}
```

> Passwords should never be stored as plain text. They must be securely hashed before being stored in the database.

---

# 3. Authorization

Authentication tells us:

> **Who is the user?**

Authorization tells us:

> **What is this user allowed to do?**

Example roles:

```text
Admin
Manager
Employee
```

Example protected request:

```http
DELETE /api/users/10
```

The request goes through:

```text
Request
   ↓
JWT Validation
   ↓
User Identity
   ↓
User Role = Employee
   ↓
Required Role = Admin
   ↓
403 Forbidden
```

### HTTP Status Codes

#### 401 Unauthorized

Usually means the user is not authenticated, or the token is missing or invalid.

#### 403 Forbidden

The user is authenticated, but does not have permission to perform the operation.

#### 500 Internal Server Error

An unexpected server-side error occurred.

---

# 4. Public vs Private APIs

Not every API should require authentication.

## Public APIs

Examples:

```text
POST /api/login
POST /api/register
GET  /api/home
```

These can be accessed without a JWT.

## Private APIs

Examples:

```text
GET    /api/users
DELETE /api/users/{id}
GET    /api/activities
POST   /api/activities
```

These require a valid authentication token.

---

# 5. JWT

JWT stands for:

**JSON Web Token**

It is commonly used to represent authenticated user information between the client and server.

A JWT can contain information such as:

```text
User ID
Username
Role
Expiration Time
```

General flow:

```text
Login
   ↓
Server validates username/password
   ↓
Generate JWT
   ↓
Return JWT
   ↓
Client stores JWT
   ↓
Client sends JWT with protected requests
   ↓
Server validates JWT
```

The token is normally sent using the HTTP Authorization header:

```http
Authorization: Bearer <token>
```

### JWT Validation

The server checks:

- Is the token valid?
- Is the signature valid?
- Has the token expired?
- Who is the user?
- What is the user's role?

---

# 6. JWT Secret

JWT signing requires a secret key.

The secret should not be hardcoded in the source code.

Bad:

```text
jwtSecret = "123456"
```

Better:

```text
JWT_SECRET=<secret-value>
```

Secrets and credentials should be stored securely, for example through environment variables or a secret-management system.

> **Never push secrets to GitHub.**

---

# 7. Password Security

Passwords should never be stored like this:

```text
username: ahmed
password: 123456
```

Instead:

```text
Password
   ↓
Hashing Algorithm
   ↓
Password Hash
   ↓
Database
```

During login:

```text
Entered Password
      ↓
Hash Verification
      ↓
Stored Hash
      ↓
Match?
```

### Hashing vs Encryption

**Hashing** is designed to be one-way.

**Encryption** is designed to be reversible using a key.

Passwords should be hashed using a suitable password-hashing algorithm rather than encrypted and stored as plaintext-equivalent data.

---

# 8. Forget Password / Reset Password

A typical password-reset flow:

```text
User clicks "Forgot Password"
          ↓
Enter email / username
          ↓
Server generates OTP
          ↓
OTP stored in database
          ↓
Expiration time stored
          ↓
Server sends OTP through email
          ↓
User enters OTP
          ↓
Server validates OTP
          ↓
OTP valid?
    ↓             ↓
   Yes            No
    ↓             ↓
Allow reset    Reject request
    ↓
Enter new password
    ↓
Hash password
    ↓
Update database
```

Example OTP record:

```text
OTP Code: 123456
User: 10
Expiration: 10 minutes
```

The OTP should have:

- Expiration time
- Limited number of attempts
- One-time usage
- Secure generation

Email delivery can be implemented using SMTP or an email provider.

---

# 9. CSRF

**CSRF = Cross-Site Request Forgery**

CSRF attempts to make an authenticated user's browser send an unwanted request to a server.

Example:

```text
User is logged in
      ↓
Attacker creates malicious website
      ↓
User visits attacker website
      ↓
Browser sends unwanted request
      ↓
Server receives request
```

CSRF protection is particularly important when authentication relies on browser cookies.

Common protections include:

- CSRF tokens
- SameSite cookies
- Secure cookies
- Origin/Referer validation

> CSRF protection should be selected according to the authentication architecture. Bearer tokens sent explicitly in the Authorization header have different CSRF characteristics from cookie-based authentication.

---

# 10. XSS

**XSS = Cross-Site Scripting**

XSS happens when an attacker manages to inject malicious JavaScript into a page that is later executed in another user's browser.

Example:

```html
<script>
    maliciousCode();
</script>
```

Common protection techniques include:

- Input validation
- Output encoding
- Content Security Policy (CSP)
- Avoiding unsafe HTML rendering
- Proper framework escaping

---

# 11. CORS

**CORS = Cross-Origin Resource Sharing**

CORS controls which origins are allowed to communicate with your API from a browser.

Example:

```text
Frontend
https://e-comm.com
        ↓
        API
        ↓
https://api.e-comm.com
```

The API can be configured to allow:

```text
https://e-comm.com
```

while rejecting unauthorized origins.

Example:

```text
Origin: https://e-comm.com
          ↓
       CORS Check
          ↓
        Allowed
```

Attacker:

```text
Origin: https://attacker.com
          ↓
       CORS Check
          ↓
        Denied
```

### Important

CORS is a **browser security mechanism**.

It is not a replacement for authentication or authorization.

A server must still properly authenticate and authorize protected API requests.

---

# 12. HTTPS / Network Security

HTTP sends data without transport encryption.

Conceptually:

```text
HTTP

Visa: 1234 5678 1234 5678
Expiry: 12/23
CVV: 123
```

With HTTPS:

```text
Client
   ↓
Encrypted HTTPS Connection
   ↓
Server
```

The purpose is to protect data while it is being transmitted over the network.

Production APIs should use:

```text
HTTPS
```

instead of plain:

```text
HTTP
```

Especially when transmitting:

- Passwords
- Authentication tokens
- Personal information
- Payment information

> HTTPS protects data in transit. It does not replace application-level security controls.

---

# 13. Rate Limiting

Rate limiting controls how many requests a client can make within a certain period.

Example:

```text
Login API

Maximum:
5 attempts / minute
```

Instead of allowing unlimited requests:

```text
Attacker
 ↓
Login
Login
Login
Login
Login
Login
Login
...
```

The server can block, reject, or slow down excessive requests.

Rate limiting helps protect against:

- Brute-force attacks
- API abuse
- Excessive traffic
- Resource exhaustion

---

# 14. API Keys

An **API Key** is another mechanism used to identify or authorize API consumers.

Example:

```http
X-API-Key: <api-key>
```

API keys are commonly useful for:

- Service-to-service communication
- Identifying API consumers
- Public/partner APIs
- Controlling API usage

An API key should not automatically be treated as a replacement for user authentication.

---

# 15. Logout

With JWT-based authentication, logout can be handled on the client by removing the stored token.

Example:

```text
User clicks Logout
       ↓
Logout function
       ↓
Delete JWT from storage
       ↓
Redirect to Login
```

For stronger security requirements, the backend can also support:

- Token revocation
- Short-lived access tokens
- Refresh tokens
- Refresh-token rotation

---

# 16. Frontend Authentication Flow

When the user opens the application:

```text
Open Home Page
      ↓
Check Authentication
      ↓
Is JWT available?
      ↓
Validate token
      ↓
 ┌───────────────┐
 │               │
Valid           Invalid
 │               │
 ↓               ↓
Continue      Login Page
```

The JWT may contain:

```text
User ID
Username
Role
Expiration
```

The frontend can use this information for UI decisions, but **the backend must still enforce authorization**.

Example:

```text
Frontend:
Hide Delete button from Employee

Backend:
Still checks whether user is Admin
```

Frontend checks improve the user experience, but they are not a security boundary.

---

# 17. Complete Security Architecture

Putting everything together:

```text
                    Client
                       │
                       ▼
                    HTTPS
                       │
                       ▼
                     CORS
                       │
                       ▼
                 Rate Limiting
                       │
                       ▼
                Authentication
                       │
                       ▼
                  JWT Validation
                       │
                       ▼
                  Authorization
                       │
                 ┌─────┴─────┐
                 │           │
              Allowed      Denied
                 │           │
                 ▼           ▼
             Controller     403
                 │
                 ▼
              Service
                 │
                 ▼
            Repository
                 │
                 ▼
              Database
```

---

# 18. Key Takeaways

## Authentication

> Who are you?

Examples:

- Login
- Register
- JWT
- Password reset

## Authorization

> What are you allowed to do?

Examples:

- Roles
- Permissions
- Protected routes

## JWT

Used to securely represent authentication information and prove the identity of the requester.

## HTTPS

Protects data in transit.

## CORS

Controls which browser origins are allowed to access the API.

## CSRF

Protects against forged requests, especially in cookie-based authentication architectures.

## XSS

Protects users from malicious scripts being executed in their browsers.

## Rate Limiting

Controls request frequency and helps reduce abuse and brute-force attacks.

## Password Hashing

Passwords should be securely hashed and never stored as plaintext.

## Secrets

JWT secrets, credentials, API keys, and other sensitive values should be stored securely and never committed to source control.
