# 🕹️ Spark Collectibles API
**Full Project – Sprint 1 to 3 | Java + Spark Framework**

---

## 🧩 Project Overview
**Spark Collectibles API** is a Java-based web application built with the **Spark Java Framework**.  
It simulates an online collectibles store where users can manage products, apply promotional offers, and view updated prices dynamically.

Originally, this project started as a REST API in **Sprint 1** (user CRUD), later evolving into a full-featured web system with templates, forms, and exception handling (Sprint 2 and 3).

---

## ⚙️ Tech Stack
| Component | Technology | Version |
|------------|-------------|----------|
| Language | Java | 22 |
| Framework | Spark Java | 2.9.4 |
| Template Engine | Mustache | 2.7.1 |
| Database | H2 (PostgreSQL mode) | 2.x |
| Logging | SLF4J Simple | 1.7.36 |
| Styles | CSS (Grid Layout + Flexbox) | — |
| Build Tool | Maven | 3.8+ |

---

## 📁 Project Structure
How to Run
# Build the project
mvn clean package

# Run the application
mvn exec:java -Dexec.mainClass="com.example.App"

# Or run from the shaded JAR
java -jar target/spark-collectibles-api-1.0.0-shaded.jar

🧩 Database Schema
-- products
id VARCHAR(40) PRIMARY KEY,
name VARCHAR(120),
descr VARCHAR(2000),
price DECIMAL(12,2),
stock INT

-- product_offers
product_id VARCHAR(40) PRIMARY KEY REFERENCES products(id),
promo_price DECIMAL(12,2),
valid_until DATE


Seed data:

ID	Name	Price	Stock
p1	Figura Goku	499.00	10
p2	Carta Pikachu	1299.00	5
🧪 Testing Endpoints (Optional)
curl -s http://localhost:4567/    # View web interface
curl -s http://localhost:4567/ping

🧾 License & Author

Author: Daneea Román
Repository: DaneeaGaby2003/Challenge_6

License: MIT (optional line)