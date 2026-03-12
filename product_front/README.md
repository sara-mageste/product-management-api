# Product Management Frontend

Frontend application developed with **Angular** to consume the **Product Management API**.

This project simulates the interface of a real-world product management system, similar to those used in **e-commerce admin panels, inventory control systems, or internal product catalogs**.

The main focus of the frontend is:
* Code organization
* Modern Angular best practices
* Efficient communication with REST APIs
* User experience (UX)
* Scalability and maintainability


## 🔍 Overview

The application allows users to manage products through a web interface by consuming data from the backend via HTTP.

Current features include:
* Product listing
* Product search by name
* Pagination
* Sorting
* Image loading via URL
* Loading and error states
* Modular architecture organized by features

The frontend was designed to be decoupled from the backend, allowing easier evolution and maintenance.


## 🚀 Features

* Product listing
* Search products by name
* Dynamic pagination
* Sorting
* REST API consumption
* Loading state handling
* Request error handling
* Structure prepared for future authentication


## 🧱 Project Architecture

The project follows a feature-based architecture, a recommended practice for scalable Angular applications.

```
src/app
│
├── core
│ ├── services → Global services (API, future interceptors)
│ └── models → Interfaces and data models
│
├── features
│ └── products
│ ├── components → Product components
│ ├── pages → Pages (lists, forms)
│ ├── service → Communication with the products API
│ └── models → Feature-specific models
│
├── shared
│ ├── components → Reusable components
│ └── styles → Shared styles
│
└── app.component.ts

```


This approach ensures:
* Clear separation of responsibilities
* Easier scalability and evolution
* More readable and testable code


## 🔗 Backend Integration

This frontend consumes the Product Management API, developed in Java with Spring Boot.

Endpoints used include:
* Product listing
* Search by name
* Pagination and sorting

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
⚠️ Make sure the backend is running for the application to work properly.

## 📈 Next Steps (Future Improvements)

🔐 Implement authentication and authorization on the frontend

📝 Create product creation and editing forms

🧪 Expand unit test coverage

🎨 Improve UX/UI and responsiveness

📦 Implement interceptors for global error handling and authentication tokens

## 👩‍💻 Author

**Sara Mageste**

Software Developer

Java • Spring Boot • Angular • APIs REST

This project was developed for study purposes and as part of a professional portfolio.