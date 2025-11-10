# 🕹️ Spark Collectibles API
**Full Project – Sprint 1 to 3 | Java + Spark Framework**

---

## 🧩 Project Overview
**Spark Collectibles API** is a Java-based web application built with the **Spark Java Framework**.  
It simulates an online collectibles store where users can **view, search, and manage promotional offers** for products.

The system evolved through three sprints:
- 🧱 **Sprint 1:** REST API for managing product data (CRUD simulation).
- 🎨 **Sprint 2:** Integration of **Mustache templates** and **HTML forms** for offer management.
- 🧪 **Sprint 3:** Added **filtering system**, **test coverage (JaCoCo)**, and **documentation** with PowerShell testing examples.

---

## ⚙️ Tech Stack
| Component | Technology | Version |
|------------|-------------|----------|
| Language | Java | 22 |
| Framework | Spark Java | 2.9.4 |
| Template Engine | Mustache | 2.7.1 |
| Database | H2 (PostgreSQL mode) | 2.2.224 |
| Logging | SLF4J Simple | 1.7.36 |
| Build Tool | Maven | 3.8+ |
| Testing | JUnit 5 + JaCoCo | — |

---

## 📁 Project Structure

spark-collectibles-api/
├── src/
│ ├── main/java/com/example/
│ │ ├── App.java
│ │ ├── Product.java
│ │ ├── ProductDao.java
│ │ ├── Offer.java
│ │ └── utils/
│ ├── main/resources/
│ │ ├── public/
│ │ │ └── styles.css
│ │ └── templates/
│ │ └── index.mustache
│ └── test/java/com/example/ProductDaoTest.java
├── pom.xml
└── README.md

🧪 How to Test (Using PowerShell)   
---
# Run server
mvn exec:java "-Dexec.mainClass=com.example.App"

# Home
Invoke-WebRequest http://localhost:4567/ | Select-Object -Expand Content | Out-Host

# Filter by name
Invoke-WebRequest "http://localhost:4567/?q=goku" | Select-Object -Expand Content | Out-Host
Invoke-WebRequest "http://localhost:4567/?q=pikachu" | Select-Object -Expand Content | Out-Host

# Create new offer
$body = @{
itemId     = 'p2'
promoPrice = '999.00'
validUntil = (Get-Date).AddDays(5).ToString('yyyy-MM-dd')
}
Invoke-WebRequest -Uri http://localhost:4567/offers -Method POST `
  -Body $body -ContentType 'application/x-www-form-urlencoded' `
-MaximumRedirection 0

# Remove offer
Invoke-WebRequest -Uri http://localhost:4567/offers/delete -Method POST `
  -Body @{ itemId = 'p1' } -ContentType 'application/x-www-form-urlencoded' `
-MaximumRedirection 0

# Health check
Invoke-WebRequest http://localhost:4567/ping | Select-Object -Expand Content

## 🚀 How to Run

```bash
# Build the project
mvn clean package

# Run the app
mvn exec:java -Dexec.mainClass="com.example.App"

The app runs at:
👉 http://localhost:4567/


| Column | Type          | Description   |
| ------ | ------------- | ------------- |
| id     | VARCHAR(40)   | Primary Key   |
| name   | VARCHAR(120)  | Product name  |
| descr  | VARCHAR(2000) | Description   |
| price  | DECIMAL(12,2) | Regular price |
| stock  | INT           | Quantity      |


#| Column      | Type          | Description              |
| ----------- | ------------- | ------------------------ |
| product_id  | VARCHAR(40)   | References `products.id` |
| promo_price | DECIMAL(12,2) | Discounted price         |
| valid_until | DATE          | Expiration date of offer |

| ID | Name          | Price   | Stock |
| -- | ------------- | ------- | ----- |
| p1 | Figura Goku   | 499.00  | 10    |
| p2 | Carta Pikachu | 1299.00 | 5     |

####🧾 License & Author

Author: Daneea Román
Repository: DaneeaGaby2003/Challenge_6

License: MIT (optional)