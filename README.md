# Tax Gap Detection & Compliance Validation Service

A Spring Boot backend service that validates financial transactions and flags gaps between expected and reported tax figures.

## Features
- REST APIs to validate transactions
- Rule-based validation logic
- Accurate tax gap calculation using BigDecimal
- Exception handling for compliance checks
- Layered architecture (Controller, Service, Repository)

## Tech Stack
Java, Spring Boot, Spring Data JPA, Hibernate, PostgreSQL, Maven

## How to Run
1. Clone the repository
2. Create a PostgreSQL database and update `src/main/resources/application.properties` with your DB name, username and password
3. Run: ./mvnw spring-boot:run
4. Test the APIs with Postman at http://localhost:8080
