# Product Management API

REST API developed in Java with Spring Boot for product management, applying best practices in architecture, data validation, global exception handling, and layered project organization.
The project simulates a real backend used in corporate applications such as e-commerce systems, inventory management, or product catalogs, and was built with a focus on code quality, maintainability, and scalability.

## 🔍 Overview

The application provides REST endpoints for complete product CRUD operations, including:

* JWT-based authentication with self-service registration
* Login attempt lockout and password recovery via email (Brevo API)
* Product management operations
* Pagination and sorting
* Search by name and code
* Product image support via URL
* Notifications management, including promotion start/end alerts
* Low stock notifications
* Promotions management with overlap validation and filtering
* Bulk product and promotion deletion
* Dynamic filtering with specifications
* Automated unit and integration tests

The API was designed with a decoupled architecture, allowing easy integration with frontend applications.
Additionally, the project already includes an initial structure prepared for authentication and security, enabling future evolution without structural refactoring.

## 🚀 Features

* JWT authentication (access + refresh tokens, with rotation)
* Self-service account registration, with a unique randomly generated employee code
* Login attempt tracking by identifier, independent of whether it exists (prevents account enumeration), with temporary lockout after repeated failures
* Password recovery via a single-use, time-limited code sent by email
* Full product CRUD operations
* Product search by name and code
* Dynamic pagination and sorting
* Dynamic filtering with JPA Specifications
* Bulk product and promotion deletion
* Notifications management
* Low stock notification support
* Unread notifications counting
* Promotions management, including overlap prevention and automatic status calculation (scheduled, active, finished)
* Data validation using Bean Validation
* Global exception handling with appropriate HTTP status codes
* Standardized error responses
* Clear separation of responsibilities by layer

## 🧱 Project Architecture

The project follows a layered architecture, ensuring separation of responsibilities and easier maintenance:

``` 
src/main/java/com/saraprojects/product_api
│
├── config        → Configuration (Spring Security)
├── controller    → REST Controllers
├── dto           → Data Transfer Objects
├── enums         → Application enums
├── exception     → Global exception handling
├── model         → JPA entities
├── repository    → Repositories (Spring Data JPA)
├── scheduler → Scheduled jobs (promotion status sync, notifications)
├── security → JWT service and authentication filter
├── service       → Business logic
├── specification → Dynamic query specifications
└── ProductApiApplication.java
``` 
This organization ensures:

* Low coupling
* High cohesion
* Easier maintenance
* Easier testing and project evolution

## 🛠️ Technologies Used

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Validation
* Spring Security + JWT (jjwt)
* Hibernate
* Lombok
* MySQL
* Maven
* JUnit 5, Mockito, H2 (in-memory database for tests)
* Brevo API (transactional email)

## 📌 Endpoints

### Authentication
```
POST /api/auth/register               → Create account (name, email, password, avatar)
POST /api/auth/login                  → Sign in with employee code + password
POST /api/auth/refresh                → Rotate access/refresh token pair
POST /api/auth/logout                 → Revoke the current refresh token
POST /api/auth/forgot-password        → Request a password reset code by email
POST /api/auth/reset-password         → Reset password using a valid reset code
```
### Products
```
POST /api/products                    → Create product
PUT /api/products/id/{id}             → Update product
DELETE /api/products/id/{id}          → Delete product
DELETE /api/products/bulk-delete      → Delete selected products
GET /api/products/id/{id}             → Get product by ID
GET /api/products                     → Get products with search, filters, pagination and sorting
GET /api/products/all                 → Get all products
```
### Promotions
```
POST /api/promotions                  → Create promotion
GET /api/promotions                   → Get promotions with pagination, sorting and filters (targetType, startDate, endDate, status)
GET /api/promotions/all               → Get all promotions (no pagination)
GET /api/promotions/id/{id}           → Get promotion by ID
DELETE /api/promotions/id/{id}        → Delete a scheduled or finished promotion
DELETE /api/promotions/bulk-delete    → Delete selected scheduled or finished promotions
```
### Notifications
```
GET /notifications                    → Get all notifications
PUT /notifications/{id}/read          → Mark notification as read
PUT /notifications/mark-all-read      → Mark all notifications as read
GET /notifications/unread/count       → Count unread notifications
GET /notifications/history            → Get notification history
DELETE /notifications/history         → Clear notification history
```
## 🔁 Business Rules

* A promotion can target either a specific product or a whole category (mutually exclusive)
* Overlapping promotions for the same product or category are not allowed
* Promotion status (SCHEDULED, ACTIVE, FINISHED) is calculated based on the current date and kept in sync via a scheduled background job
* Active promotions cannot be deleted, individually or in bulk
* A single notification is sent when a promotion both starts and ends "tomorrow" (single-day promotion), instead of two separate ones
* Login attempts are tracked by the entered employee code itself, regardless of whether it exists, preventing unlimited brute-force enumeration of codes
* The login/register/reset-password responses never reveal whether a given code or email exists in the system
* After a password reset, all active sessions for that account are revoked

## ✅ Data Validation

The API uses Bean Validation to ensure the integrity of incoming data:

* Name, email and password are required on registration; passwords must be at least 8 characters
* Price is required and must be greater than zero
* Quantity is required and must be greater than or equal to zero
* URL format validation for product images
* Discount percentage must be between 0 and 100

Invalid requests return clear and structured error messages, making it easier for frontend applications to consume the API.

## ⚠️ Exception Handling

The project uses a global exception handling mechanism (GlobalExceptionHandler), ensuring:

* Standardized error responses
* Clear validation error messages
* Proper use of HTTP status codes

## 🔐 Security

The application uses Spring Security with a stateless JWT-based authentication filter chain.

Current state:
* `/api/auth/**` endpoints are public; all other endpoints require a valid access token
* Passwords are hashed with BCrypt
* Access tokens are short-lived; refresh tokens are rotated on each use and can be revoked
* CSRF disabled (stateless API)
* CORS restricted to the configured frontend origin

## 🧪 Testing

The project includes an automated test suite covering authentication, promotions and product pricing logic:

* Unit tests (JUnit 5 + Mockito) for services, covering business rules such as login lockout, password reset, promotion overlap validation, and discount calculation precision
* Integration tests (MockMvc + H2 in-memory database) covering controller HTTP responses and protected-endpoint authorization

Run the tests with:
```
mvn test
```

## 🔒 Sensitive Configuration

No sensitive credentials are stored in the repository.
Configuration is handled through environment variables:
```
DB_URL
DB_USER
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
JWT_REFRESH_EXPIRATION
BREVO_API_KEY
BREVO_SENDER_EMAIL
BREVO_SENDER_NAME
```

Sensitive files are ignored using .gitignore.

## ▶️ Running the Project

### Prerequisites

* Java 21
* Maven
* MySQL

### Steps

1. Clone the repository
2. Configure the environment variables
3. Create a MySQL database
4. Run the application:
```
mvn spring-boot:run
```
The API will be available at:
```
http://localhost:8080
```

## 📈 Next Steps (Future Improvements)

🤖 Add CAPTCHA to account registration

📄 Document the API using Swagger/OpenAPI

☁️ Deploy (free tier)

# 👩‍💻 Author

**Sara Mageste**

Software Developer

Java • Spring Boot • APIs REST • Lombok

Project developed for study and professional portfolio.

