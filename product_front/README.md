# Product Management Frontend

Frontend application developed with **Angular** to consume the **Product Management API**.

The project simulates the interface of a real-world product management system, similar to those used in inventory management platforms, internal admin dashboards, and e-commerce product control panels.

The project focuses on:
* Scalable Angular architecture
* Component reusability
* REST API integration
* Clean UI organization
* Maintainability and code readability


## 🔍 Overview

The application provides a complete frontend interface for product management operations, including:

* JWT-based authentication, with automatic token attachment and silent refresh on expiration
* Self-service account creation, with a preset avatar picker
* Login with attempt lockout and animated visual feedback
* Password recovery via a one-time code sent by email
* Product management operations
* Dynamic pagination and sorting
* Product search by name
* Bulk product deletion
* Notifications management
* Low stock notifications
* Promotions management, with filters, bulk delete and a visual discount tag on product cards
* Product image support via URL
* Side navigation menu, showing the real logged-in user's data
* About/Profile section
* Reusable modal components

The frontend was designed using a feature-based architecture, allowing easier scalability, maintenance, and integration with backend services.

Additionally, the project communicates with a Java Spring Boot REST API, maintaining a decoupled architecture between frontend and backend layers.


## 🚀 Features

* Login, self-service registration and password recovery
* Automatic session renewal via refresh tokens
* Route guards protecting authenticated pages
* Product listing
* Product creation
* Product editing
* Product deletion
* Bulk product deletion
* Product search by name
* Dynamic pagination and sorting
* Side navigation menu
* Notifications management
* Low stock notification support
* Promotions management with filters and bulk delete
* About/Profile section
* Reusable modal components
* Loading state handling
* Request error handling
* REST API integration
* Component-based architecture organization


## 🧱 Project Architecture

The project follows a feature-based architecture, organizing related files by business domain.

```
src/app 
│ 
├── features/products 
│ 
├── about-me              → Profile/About section 
├── confirmation-modal    → Confirm/cancel action modal 
├── enums                 → Application enums 
├── guards → Route guards (auth)
├── interceptors → HTTP interceptors (auth token, refresh)
├── login → Login, registration and password recovery
├── models                → Interfaces and data models 
├── notifications         → Notifications feature 
├── product-card          → Product card component 
├── home                  → Product listing 
├── product-modal         → Create/Edit product modal 
├── promotions            → Promotions feature  
├── service               → API communication layer 
└── side-menu             → Side navigation menu

```


This architecture provides:
* Better feature isolation
* Easier maintenance
* Component reusability
* Cleaner scalability for future modules


## 🔗 Backend Integration

This frontend consumes the Product Management API, developed in Java with Spring Boot.

Endpoints currently used include:
* Authentication (login, registration, refresh, logout, password recovery)
* Product listing, creation, editing, deletion and bulk deletion
* Product search, pagination and sorting
* Notifications management
* Promotions management, including filtering and bulk deletion

Backend base URL:

```
http://localhost:8080
```

## 🛠️ Technologies Used

- Angular 21
- TypeScript
- RxJS
- Angular HttpClient
- Angular Router
- HTML5
- CSS3
- Node.js
- Angular CLI


## ▶️ Running the Project

### Prerequisites
* Node.js (version compatible with Angular 21)
* Angular CLI

### Steps

1. Clone the repository
2. Navigate to the frontend folder:
```bash
cd product_front
```
3. Install dependencies:
```
npm install
```

4. Run the project:
```
ng serve
```

The application will be available at:
```
http://localhost:4200
```
⚠️ Make sure the backend is running before starting the frontend application.

## 📈 Next Steps (Future Improvements)

🤖 Add CAPTCHA to account registration

🧪 Add automated frontend test coverage

☁️ Deploy (free tier)

## 👩‍💻 Author

**Sara Mageste**

Software Developer

Java • Spring Boot • Angular • APIs REST

This project was developed for study purposes and as part of a professional portfolio.