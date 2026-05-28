# Product Management API

REST API developed in Java with Spring Boot for product management, applying best practices in architecture, data validation, global exception handling, and layered project organization.
The project simulates a real backend used in corporate applications such as e-commerce systems, inventory management, or product catalogs, and was built with a focus on code quality, maintainability, and scalability.

## 🔍 Overview

The application provides REST endpoints for complete product CRUD operations, including:

* Product management operations
* Pagination and sorting
* Search by name and code
* Product image support via URL
* Notifications management
* Low stock notifications
* Bulk product deletion
* Dynamic filtering with specifications

The API was designed with a decoupled architecture, allowing easy integration with frontend applications.
Additionally, the project already includes an initial structure prepared for authentication and security, enabling future evolution without structural refactoring.

## 🚀 Features

* Full product CRUD operations
* Product search by name and code
* Dynamic pagination and sorting
* Dynamic filtering with JPA Specifications
* Bulk product deletion
* Notifications management
* Low stock notification support
* Unread notifications counting
* Data validation using Bean Validation
* Global exception handling
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
* Spring Security (initial configuration)
* Hibernate
* Lombok
* MySQL
* Maven

## 📌 Endpoints
```
POST /api/products                    → Create product 
PUT /api/products/id/{id}             → Update product 
DELETE /api/products/id/{id}          → Delete product 
DELETE /api/products/bulk-delete      → Delete selected products 
GET /api/products/id/{id}             → Get product by ID 
GET /api/products                     → Get products with search, filters, pagination and sorting 
GET /api/products/all                 → Get all products 
GET /notifications                    → Get notifications 
PUT /notifications/{id}/read          → Mark notification as read 
GET /notifications/unread/count       → Count unread notifications

```

## ✅ Data Validation

The API uses Bean Validation to ensure the integrity of incoming data:

* Name is required
* Price is required and must be greater than zero
* Quantity is required and must be greater than or equal to zero
* URL format validation for product images

Invalid requests return clear and structured error messages, making it easier for frontend applications to consume the API.

## ⚠️ Exception Handling

The project uses a global exception handling mechanism (GlobalExceptionHandler), ensuring:

* Standardized error responses
* Clear validation error messages
* Proper use of HTTP status codes

## 🔐 Security

The application uses Spring Security with an initial configuration.

Current state:
* All endpoints are allowed (permitAll)
* CSRF disabled (stateless API)
* Structure prepared for future authentication

The architecture allows easy evolution to JWT-based authentication without structural refactoring.

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

🔔 Improve notifications management

🔐 Implement authentication and authorization using Spring Security + JWT

🧪 Add unit and integration tests

📄 Document the API using Swagger/OpenAPI

# 👩‍💻 Author

**Sara Mageste**

Software Developer

Java • Spring Boot • APIs REST • Lombok

Project developed for study and professional portfolio.

