# Product Management Frontend

Frontend application developed with **Angular** to consume the **Product Management API**.

This project simulates the interface of a real-world product management system, similar to those used in **inventory management, internal admin dashboards, and e-commerce product control panels**.

The project focuses on:
* Scalable Angular architecture
* Component reusability
* REST API integration
* Clean UI organization
* Maintainability and code readability


## 🔍 Overview

The application simulates a modern product management dashboard connected to a REST API.

It was built using Angular with a feature-based architecture focused on scalability, reusable components, and maintainable code organization.

The frontend communicates with a Java Spring Boot backend and provides a modular structure for managing products, notifications, navigation, and future application features.


## 🚀 Features

* Product listing
* Create products
* Edit products
* Delete products
* Search products by name
* Dynamic pagination
* Product sorting
* Notifications system
* Reusable modal components
* Side navigation menu
* About/Profile section
* REST API consumption
* Loading state handling
* Request error handling


## 🧱 Project Architecture

The project follows a feature-based architecture, organizing related files by business domain.

```
src/app 
│ 
├── features/products 
│ 
├── about-me              → Profile/About section 
├── enums                 → Application enums 
├── models                → Interfaces and data models 
├── notifications         → Notifications feature 
├── product-card          → Product card component 
├── product-delete-modal  → Delete confirmation modal 
├── product-list          → Product listing 
├── product-modal         → Create/Edit product modal 
├── service               → API communication layer 
└── side-menu             → Side navigation menu

```


* This architecture provides:
* Better feature isolation
* Easier maintenance
* Component reusability
* Cleaner scalability for future modules


## 🔗 Backend Integration

This frontend consumes the Product Management API, developed in Java with Spring Boot.

Endpoints currently used include:
* Product listing
* Product creation
* Product editing
* Product deletion
* Product search
* Pagination and sorting
* Notifications management

Backend base URL:

```
http://localhost:8080/api/products
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

🔐 Implement authentication and authorization on the frontend

📍 Implement notification history

📝 Implement promotions

🧪 Expand unit test coverage

📦 Implement interceptors for global error handling and authentication tokens

## 👩‍💻 Author

**Sara Mageste**

Software Developer

Java • Spring Boot • Angular • APIs REST

This project was developed for study purposes and as part of a professional portfolio.