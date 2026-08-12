# 🩺 Bio-Track – Smart Health Monitoring System

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> **Bio-Track** is a Java-based health monitoring and personal health record management system developed using **Java, JavaFX, MySQL, JDBC, and PDF generation tools**.

The project was initially developed as a **console-based health monitoring application** and was later upgraded into a **JavaFX-based graphical application** with database integration, authentication, health-record management, medical document management, and report generation.

---

## 📖 About the Project

**Bio-Track** is a health monitoring system designed to help users store, manage, and review their personal health information.

The system allows users to maintain records such as:

* ❤️ Heart Rate
* 💉 Blood Pressure
* 🍬 Blood Sugar
* 🌡️ Temperature
* ⚖️ Weight
* 📏 Height
* 🧮 BMI
* 🩺 Other health-related information

The project has two implementations:

### 🖥️ 1. ProjectHealth – Console-Based Version

The `ProjectHealth` folder contains the **original console-based implementation** of Bio-Track.

This version was developed to demonstrate the initial concept and includes features such as:

* Console-based user interaction
* Health record management
* MySQL database integration
* Health calculations and analysis
* Multilingual support

The console version supports multiple languages:

* 🇬🇧 English
* 🇮🇳 Hindi
* 🇮🇳 Bengali
* 🇮🇳 Tamil
* 🇮🇳 Telugu

### 🖥️ 2. JavaFX Version – Current Upgraded Application

The rest of the repository contains the **upgraded JavaFX-based version** of Bio-Track.

The JavaFX version provides a graphical user interface and extends the original project with features such as:

* 🔐 User authentication and login
* 👤 User profile management
* 🩺 Health record management
* 📊 Health data analysis
* 📁 Medical document storage
* 📄 PDF report generation
* 💾 MySQL database persistence
* 🖥️ Modern JavaFX graphical interface

> **Note:** `ProjectHealth` is the original console-based version. The remaining project files represent the newer JavaFX-based implementation.

---

## ✨ Key Features

### 🔐 Authentication

* User registration and login
* User-specific health records
* Secure password handling
* Session-based user access

### 🩺 Health Record Management

Users can store and manage important health information including:

* Heart rate
* Blood pressure
* Blood sugar
* Temperature
* Weight
* Height
* BMI

### 📊 Health Analysis

Bio-Track analyzes recorded health parameters and provides basic health-status information based on configured reference ranges.

### 📁 Medical Document Management

Users can manage medical documents such as:

* PDF files
* Images
* Medical reports
* Prescriptions
* Other health-related documents

Available operations include:

* Upload
* View
* Download
* Delete

### 📄 PDF Reports

The JavaFX version can generate health reports containing recorded health information and analysis.

### 👤 Profile Management

Users can maintain personal information such as:

* Name
* Age
* Gender
* Blood group
* Emergency contact
* Other profile information

---

## 🏗️ Project Structure

```text
Bio-Track/
│
├── ProjectHealth/
│   └── Original Console-Based Version
│
├── src/
│   ├── DAO/
│   ├── Model/
│   ├── Service/
│   ├── UI/
│   └── DB/
│
├── resources/
│   └── CSS / UI resources
│
├── database/
│   └── Database / SQL files
│
├── README.md
└── LICENSE
```

### Architecture

The JavaFX application follows a layered structure:

```text
JavaFX UI
    ↓
Service Layer
    ↓
DAO Layer
    ↓
MySQL Database
```

This separation helps keep the user interface, business logic, and database operations organized and maintainable.

---

## 🛠️ Tech Stack

### Current JavaFX Version

| Technology     | Purpose                   |
| -------------- | ------------------------- |
| ☕ Java         | Core programming language |
| 🎨 JavaFX      | Graphical User Interface  |
| 🗄️ MySQL      | Database                  |
| 🔌 JDBC        | Database connectivity     |
| 📄 PDF Library | PDF report generation     |
| 🎨 CSS         | JavaFX UI styling         |
| 🔐 BCrypt      | Password hashing          |

### Original Console Version

| Technology           | Purpose                   |
| -------------------- | ------------------------- |
| ☕ Java               | Core programming language |
| 🗄️ MySQL            | Database                  |
| 🔌 JDBC              | Database connectivity     |
| 🌍 Java localization | Multilingual support      |

---

## 🗃️ Database

Bio-Track uses **MySQL** for persistent storage.

The database stores information such as:

* User accounts
* User profiles
* Health records
* Medical documents
* Related health information

Make sure MySQL is running before starting the application.

---

## 🚀 How to Run

### Prerequisites

Make sure the following are installed:

* Java JDK
* JavaFX SDK
* MySQL
* MySQL Connector/J
* Required project dependencies
* Eclipse or IntelliJ IDEA

### 1. Clone the Repository

```bash
git clone https://github.com/Devesh517/Bio-Track.git
```

### 2. Open the Project

Open the project in **Eclipse** or **IntelliJ IDEA** and configure the required JavaFX libraries.

### 3. Configure MySQL

Start MySQL and create the required Bio-Track database.

Import the SQL/database script provided in the repository if available.

### 4. Configure Database Connection

Update the database configuration with your local MySQL credentials.

Example:

```text
Host: localhost
Port: 3306
Database: <database_name>
Username: <your_username>
Password: <your_password>
```

### 5. Run the Application

Run the JavaFX main application class to start the graphical version.

---

## 🖥️ Versions

| Version  | Location            | Interface  | Status                  |
| -------- | ------------------- | ---------- | ----------------------- |
| Original | `ProjectHealth/`    | Console    | Previous implementation |
| Current  | Main JavaFX project | JavaFX GUI | Upgraded implementation |

The JavaFX version is the **primary/current version** of Bio-Track, while `ProjectHealth` has been retained to preserve the original console-based implementation and project evolution.

---

## 🎯 Project Evolution

Bio-Track was developed incrementally:

```text
Console-Based Application
        ↓
MySQL Database Integration
        ↓
Health Record Management
        ↓
JavaFX GUI
        ↓
Authentication & Profiles
        ↓
Medical Document Management
        ↓
PDF Report Generation
        ↓
Health Analysis
```

This evolution demonstrates the transition from a basic console application into a more complete graphical health-record management system.

---

## 👨‍💻 Development

**Bio-Track** was developed as an academic project during the **Identity Exhibition** with my teammate **Priyanshu**.

### Developer

**Devesh Dhanwani**

---

## 📜 License

This project is licensed under the **MIT License**.

See the [LICENSE](LICENSE) file for details.
