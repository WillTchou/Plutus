# Plutus

**Plutus** is a modern, event-driven payment processing engine designed to simulate the core responsibilities of a real fintech payment processor.

It focuses on **reliability**, **security**, and **scalability**, while demonstrating best practices used in production-grade payment systems.

> ⚠️ Educational & demonstrative project — not intended for production use.

---

## ✨ Key Features

- Asynchronous payment processing (event-driven)
- Idempotent transaction handling
- Account & balance management
- Fraud detection rules
- Transaction audit & lifecycle tracking
- Admin dashboard with real-time KPIs
- Secure API with JWT authentication

---

## 🧠 Architecture Overview

Plutus is built around an **event-driven architecture**, inspired by real payment processors (Stripe, Adyen, banking cores).

### High-level flow

1. A transaction request is received via REST API
2. The transaction is validated and stored as `PENDING`
3. A payment event is published to Kafka
4. The payment processor consumes the event
5. Business & fraud rules are applied
6. The transaction status is updated (`SUCCESS` / `FAILED`)
7. All steps are auditable and traceable

---

## 🧩 Core Concepts Implemented

### 🔁 Asynchronous Processing
- Kafka is used to decouple transaction intake from processing
- Improves scalability and fault tolerance
- Mirrors real-world payment processing pipelines

### 🔑 Idempotency
- Each transaction supports an **Idempotency Key**
- Prevents duplicate payments caused by retries
- Same request → same result (Stripe-like behavior)

### 🛡️ Fraud Rules (simple but realistic)
- More than 5 transactions per minute → blocked
- Amount greater than 1000 € → rejected
- New account (< 7 days) with high amount → rejected

### 📜 Audit & Traceability
- Every transaction step is logged
- Status transitions are traceable
- Designed to reflect compliance requirements (PCI-like mindset)

---

## 🖥️ Admin Dashboard

The Angular dashboard provides:

- Total processed volume
- Success / failure rate
- Average processing time
- Latest transactions
- Transaction status visualization
- Account balances overview

Design principles:
- Minimalist
- Data-focused
- Inspired by fintech internal back-office tools

---

## 🛠️ Tech Stack

### Backend
- **Java 21**
- **Spring Boot**
- Spring Web
- Spring Data JPA
- Spring Security (JWT)
- Hibernate Validator

### Messaging
- **Apache Kafka**

### Database
- **PostgreSQL**

### Frontend
- **Angular 21**
- Angular Material (customized theme – Indigo)
- Chart.js

### Infrastructure
- Docker
- Docker Compose
- GitHub Actions (CI)

---
