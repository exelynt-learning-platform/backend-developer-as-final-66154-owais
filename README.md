# Resource Booking System

A RESTful API for booking resources (rooms, vehicles, equipment) built with Spring Boot, Spring Security, JWT, and PostgreSQL. It provides role-based access control, full CRUD operations, filtering, pagination, and sorting.

## Features

- JWT-based authentication (`POST /auth/login`)
- Roles: `ADMIN` and `USER`
- Full CRUD for resources (ADMIN only)
- Users can view resources (read-only) and create reservations
- Users can view only their own reservations; admins can view all
- Reservation statuses: `PENDING`, `CONFIRMED`, `CANCELLED`
- Reservation filtering by status, minimum price, and maximum price
- Pagination and sorting (`page`, `size`, `sort`)
- Validation and proper error responses
- Swagger/OpenAPI documentation
- Seed users for testing
- Docker support (PostgreSQL + application)

## Technologies

- Java 17+
- Spring Boot 3.x
- Spring Security
- JWT (JSON Web Tokens)
- Spring Data JPA / Hibernate
- PostgreSQL
- Lombok
- Springdoc OpenAPI (Swagger UI)
- Maven
- Docker (optional)

## Prerequisites

- JDK 17 or higher
- Maven 3.8+
- PostgreSQL (or Docker for containerized database)

## Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/exelynt-learning-platform/backend-developer-as-final-66154-owais.git
cd backend-developer-as-final-66154-owais

2. Set up the database
Option A: Using Docker (recommended)
docker run --name booking-postgres \
  -e POSTGRES_DB=booking_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=yourpassword \
  -p 5432:5432 \
  -d postgres:16-alpine

  Option B: Local PostgreSQL installation
Install PostgreSQL
Create a database named booking_db
Note the username and password

3. Configure application.properties
Edit src/main/resources/application.properties:

properties
spring.datasource.url=jdbc:postgresql://localhost:5432/booking_db
spring.datasource.username=postgres
spring.datasource.password=owais
app.jwt.secret=YourSuperSecretKeyForJWTGenerationThatIsAtLeast256BitsLong
app.jwt.expiration-ms=86400000

4. Run the application
Using Maven wrapper
bash
./mvnw spring-boot:run
On Windows: mvnw.cmd spring-boot:run

Using JAR
bash
./mvnw clean package -DskipTests
java -jar target/booking-system-0.0.1-SNAPSHOT.jar
The API will start at http://localhost:8080.

API Documentation
Swagger UI is available at:

text
http://localhost:8080/swagger-ui.html
The raw OpenAPI JSON can be accessed at /v3/api-docs.

Default Users
The application seeds two users on startup:

Username	Password	    Roles
admin	    admin123	  ROLE_ADMIN
user	    user123	      ROLE_USER

API Endpoints

Authentication
POST /auth/login – Authenticate and receive a JWT token

Resources
GET /api/resources – Get all resources (ADMIN, USER)
GET /api/resources/{id} – Get resource by ID (ADMIN, USER)
POST /api/resources – Create resource (ADMIN only)
PUT /api/resources/{id} – Update resource (ADMIN only)
DELETE /api/resources/{id} – Delete resource (ADMIN only)

Reservations

GET /api/reservations – Get reservations (ADMIN: all, USER: own)
Optional query parameters: status, minPrice, maxPrice, page (default 0), size (default 10), sort (e.g., price,desc)
GET /api/reservations/{id} – Get reservation by ID (owner or admin)
POST /api/reservations – Create a reservation (USER, ADMIN)
PUT /api/reservations/{id} – Update reservation status/time/price (ADMIN only)
DELETE /api/reservations/{id} – Delete/cancel reservation (ADMIN only)

Project Structure

src/main/java/com/example/bookingsystem/
├── config/          # Security, Swagger, data seeding
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA entities
├── exception/       # Custom exceptions and global handler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT utilities and user details
└── service/         # Business logic

Testing

To run tests:

bash
./mvnw test
windows
mvn test