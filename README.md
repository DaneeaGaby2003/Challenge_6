# 🕹️ Spark Collectibles API
**Full Project – Sprint 1 to 3 | Java + Spark Framework**

---

## 🧩 Project Overview

**Spark Collectibles API** is a Java-based web application built with the **Spark Java Framework**.  
It simulates an online collectibles store where users can **view, search, and manage promotional offers** for products in real time.

The system evolved through three main development sprints:

- 🧱 **Sprint 1:** Initial **REST API** for managing product data (CRUD simulation).
- 🎨 **Sprint 2:** Integration of **Mustache templates**, **HTML forms**, and **error handling** for managing offers visually.
- 🧪 **Sprint 3:** Added **search filters**, **offer removal feature**, **automated testing (JUnit)**, **coverage reports (JaCoCo)**, and full **documentation** with PowerShell testing examples.

---

## ⚙️ Tech Stack

| Component | Technology | Version |
|------------|-------------|----------|
| Language | Java | 22 |
| Framework | Spark Java | 2.9.4 |
| Template Engine | Mustache | 2.7.1 |
| Database | H2 (PostgreSQL Mode) | 2.2.224 |
| Logging | SLF4J Simple | 1.7.36 |
| Build Tool | Maven | 3.8+ |
| Testing | JUnit 5 + JaCoCo | — |

---
## 💡 Features Implemented
| **Feature**         | **Description**                                                   |
| ------------------- | ----------------------------------------------------------------- |
| 🗂️ Product Listing | Displays all products in the database with price and description. |
| 🔎 Search Filter    | Users can filter by product name or description.                  |
| 💰 Manage Offers    | Operators can create or update promotional prices using a form.   |
| ❌ Delete Offers     | Existing offers can be removed via the web interface.             |
| ⚠️ Error Handling   | 400, 404, and 500 routes display friendly messages.               |
| ✅ Health Check      | `GET /ping` confirms server availability.                         |
| 🧪 Unit Tests       | `ProductDaoTest` verifies CRUD and filtering logic.               |
| 📊 Coverage         | Integrated **JaCoCo** plugin generates HTML coverage report.      |

## 📊 User Stories Summary
|   **ID**  | **User Story**                                                 | **Acceptance Criteria**                           | **Sprint** |
| :-------: | :------------------------------------------------------------- | :------------------------------------------------ | :--------: |
| **US-01** | As a Visitor, I want the service to respond to a health check. | `GET /ping` returns "pong".                       |      1     |
| **US-02** | As a Visitor, I want to see stored products.                   | Seed data appears in the interface.               |      1     |
| **US-03** | As a Visitor, I want to see product list visually.             | `GET /` renders Mustache HTML with product cards. |      2     |
| **US-04** | As an Operator, I want to create or update offers.             | `POST /offers` saves promo data, redirects 302→/. |      2     |
| **US-05** | As a Visitor, I want to see promo price and validity.          | Card shows discounted price and expiration date.  |      2     |
| **US-06** | As a User, I want error handling for invalid actions.          | Returns 400, 404, or 500 pages.                   |      2     |
| **US-07** | As a Visitor, I want to filter products by name.               | `GET /?q=text` filters products dynamically.      |      3     |
| **US-08** | As an Operator, I want to remove existing offers.              | `POST /offers/delete` removes offer successfully. |      3     |
| **US-09** | As a Developer, I want automated test coverage.                | JaCoCo report generated with JUnit.               |      3     |
| **US-10** | As a Reviewer, I want clear documentation and diagrams.        | README includes setup, usage, and testing info.   |      3     |

## 🧭 Sprint 3 Key Deliverables
| Deliverable                   | Description                                         |
| ----------------------------- | --------------------------------------------------- |
| ✅ **Functional Filtering**    | Search bar filters by product name or description.  |
| ✅ **Offer Management System** | Allows creation, update, and removal of offers.     |
| ✅ **Testing Integration**     | Unit tests verify DAO logic with coverage reports.  |
| ✅ **Documentation**           | README and diagrams included for developer clarity. |

## 📈 Flow Diagram – Offer Creation and Search
User fills Offer Form
│
▼
[POST /offers]
│
▼
Validate Input
│
├─ Invalid → return 400 + error message
│
▼
Check product exists (DAO.findById)
│
▼
Insert/Update offer in DB
│
▼
Redirect → "/" (Home)
│
▼
Mustache re-renders product list
(showing promo price + validUntil)

## 🧮 System Architecture Diagram (ASCII version for README)
                   ┌────────────────────────────┐
                   │        User / Browser       │
                   │────────────────────────────│
                   │  • Visits / (Home Page)     │
                   │  • Submits Offer Form       │
                   │  • Searches by name (q=)    │
                   └──────────────┬─────────────┘
                                  │  HTTP Requests (GET / POST)
                                  ▼
         ┌────────────────────────────────────────────────────────┐
         │                 Spark Java Server (App.java)           │
         │────────────────────────────────────────────────────────│
         │ • Defines routes: "/", "/offers", "/offers/delete"     │
         │ • Handles forms & filtering logic                      │
         │ • Renders HTML with Mustache templates                 │
         │ • Returns 400 / 404 / 500 pages if needed              │
         └──────────────┬────────────────────────────────────────┘
                        │
                        │ DAO Calls (SQL)
                        ▼
        ┌───────────────────────────────────────────────┐
        │                ProductDao.java                │
        │───────────────────────────────────────────────│
        │ • Connects to H2 database                     │
        │ • findAll(), findAllFiltered(q)               │
        │ • saveOrUpdateOffer(), deleteOffer()          │
        │ • findById()                                  │
        └──────────────┬────────────────────────────────┘
                       │ JDBC
                       ▼
          ┌────────────────────────────────────────┐
          │              H2 Database                │
          │────────────────────────────────────────│
          │ Tables:                                │
          │  - products (id, name, descr, price)    │
          │  - product_offers (promo_price, date)   │
          └────────────────────────────────────────┘

## 📁 Project Structure

```bash
spark-collectibles-api/
├── src/
│   ├── main/java/com/example/
│   │   ├── App.java                # Main server (routes + templates)
│   │   ├── Product.java            # Model class
│   │   ├── ProductDao.java         # DAO with SQL logic
│   │   ├── Offer.java              # Offer data model
│   ├── main/resources/
│   │   ├── public/
│   │   │   └── styles.css          # Basic CSS styling
│   │   └── templates/
│   │       └── index.mustache      # Web interface template
│   └── test/java/com/example/
│       └── ProductDaoTest.java     # JUnit test cases
├── pom.xml                         # Maven configuration
└── README.md

Author: Daneea Román
Repository: Challenge_6
License: MIT (optional)