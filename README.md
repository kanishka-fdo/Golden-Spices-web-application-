# 🌶️ Golden Spices Web Application

## 📌 Project Overview

Golden Spices Web Application is a **Spring Boot-based e-commerce system** developed for **Golden Dissanayaka Distributors**.
This platform allows users to browse, purchase, and manage premium Sri Lankan spices online.

---

## 🚀 Features

* 🛒 Product browsing and purchasing
* 🔐 User authentication & authorization (Spring Security)
* 📦 Order management system
* 💳 Online payment integration (Stripe)
* 📄 Invoice generation (PDF)
* 🗄️ Database integration (SQL Server)
* 🌐 Dynamic web pages using Thymeleaf

---

## 🛠️ Technologies Used

* **Backend:** Spring Boot (Java 17)
* **Frontend:** Thymeleaf, HTML, CSS
* **Security:** Spring Security
* **Database:** Microsoft SQL Server
* **ORM:** Spring Data JPA
* **Build Tool:** Maven
* **Payment Gateway:** Stripe API
* **PDF Generation:** iText

---

## 📂 Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/          # Java source code
│   │   ├── resources/     # Templates, static files, configs
│
├── .mvn/
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## ⚙️ Setup Instructions

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/kanishka-fdo/Golden-Spices-web-application-.git
cd Golden-Spices-web-application-
```

### 2️⃣ Configure Database

* Install **SQL Server**
* Create a database
* Update `application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=your_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

### 3️⃣ Run the Application

Using Maven:

```bash
mvn spring-boot:run
```

OR using wrapper:

```bash
./mvnw spring-boot:run
```

---

### 4️⃣ Access the Application

Open browser:

```
http://localhost:8080
```

---

## 🔐 Default Functionalities

* User registration & login
* Secure authentication system
* Role-based access control

---

## 💳 Payment Integration

* Integrated with **Stripe API**
* Secure online transactions supported

---

## 📄 PDF Features

* Generates invoices using **iText PDF library**

---

## 📸 Screenshots

*Add your website screenshots here*

---

## 👨‍💻 Author

* **Kanishka Fernando**
* GitHub: https://github.com/kanishka-fdo

---

## 📜 License

This project is for educational purposes.

---

## ⭐ Support

If you like this project:

* ⭐ Star the repository
* 🍴 Fork it
* 🛠️ Contribute improvements

---
