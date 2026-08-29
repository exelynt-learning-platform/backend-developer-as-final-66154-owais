# Resource Booking System

A RESTful API for booking resources (rooms, vehicles, equipment) built with Spring Boot, Spring Security, JWT, and PostgreSQL. It supports role-based access control (ADMIN and USER), full CRUD operations, filtering, pagination, and sorting.

## Features

- JWT-based authentication (`POST /auth/login`)
- Role-based authorization: `ADMIN` and `USER`
- Full CRUD for resources (ADMIN only)
- User can view resources (read-only) and create reservations
- Users can view only their own reservations; admins can view all
- Reservation statuses: `PENDING`, `CONFIRMED`, `CANCELLED`
- Reservation filtering by status, minimum price, and maximum price
- Pagination and sorting using `page`, `size`, and `sort` parameters
- Validation and proper error responses
- API documentation with Swagger/OpenAPI
- Seed users for testing
- Docker support for PostgreSQL and the application

## Technologies Used

- Java 17+
- Spring Boot 3.x
- Spring Security
- JWT (JSON Web Tokens)
- Spring Data JPA / Hibernate
- PostgreSQL (or MySQL)
- Lombok
- Springdoc OpenAPI (Swagger UI)
- Maven
- Docker (optional)

## Prerequisites

- JDK 17 or higher
- Maven 3.8+
- PostgreSQL 14+ (if running without Docker)
- Docker (optional, for containerized setup)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/Owaisbewnak/Booking-System.git
cd Booking-System
