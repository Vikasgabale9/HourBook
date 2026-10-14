# HourBook — Sports Ground Booking Platform

**HourBook** is a Java/Spring Boot web application for discovering sports grounds and managing hourly reservations. It provides separate workflows for users, vendors, and administrators, with server-side booking validation, JWT-secured REST APIs, session-protected web dashboards, and Razorpay payment verification.

- **Live application:** https://hourbook.onrender.com/
- **Repository:** https://github.com/Vikasgabale9/HourBook
- **Developer:** [Vikas Gabale](https://github.com/Vikasgabale9)

> This repository is a portfolio/demo project. Configure your own database and provider credentials before running it locally. Availability and payment behavior in the hosted demo may depend on the deployment and external services.

## Features

### User
- Register, log in, and verify accounts with OTP.
- Browse and search sports grounds and view details, images, amenities, operating hours, and hourly prices.
- Check available time slots and create bookings.
- Pay through Razorpay and view booking history/status.
- Manage profile details and cancel eligible bookings.

### Vendor
- Register, log in, and manage a vendor profile.
- Submit KYC information and documents and view KYC status.
- Add, edit, activate/deactivate, and view grounds.
- Configure sport, ground type, location, price, operating hours, slot duration, amenities, and images.
- View bookings for vendor-owned grounds and view booking/revenue summaries.

### Admin
- Access the admin dashboard.
- Manage users and vendors.
- Review vendor KYC and approve/reject grounds.
- View and manage grounds and bookings.

## Booking and payment flow

1. The user selects a ground, date, and time slot.
2. The server validates the ground's status, operating hours, requested date/time, slot duration, and existing bookings.
3. A booking is created in a pending state and a Razorpay order is created.
4. The client completes Razorpay Checkout.
5. The server validates the returned order/payment details and verifies the Razorpay HMAC-SHA256 signature.
6. The booking is confirmed only after successful server-side payment verification.

The browser is not treated as the authority for payment success. Pending and confirmed bookings are considered when checking slot conflicts. The application also has configurable expiry settings for stale pending bookings.

## Authentication and security

The application has both REST APIs and server-rendered Thymeleaf pages:

- **REST APIs:** Spring Security, JWT authentication, and role-based authorization.
- **Web dashboards:** session checks for user/vendor/admin pages; admin routes also use an interceptor.
- **Payments:** server-side Razorpay signature verification.
- **Secrets:** database passwords, JWT signing secret, payment credentials, and mail credentials should be supplied through environment variables, not committed to source control.

Do not use real provider credentials in screenshots, issues, commits, or sample configuration files. If a credential was ever committed, rotate it; removing it from the latest commit alone does not invalidate it.

## Architecture

HourBook uses a layered Spring Boot architecture:

```text
Browser (HTML / CSS / JavaScript / Thymeleaf)
                    |
                    v
          Web Controllers / REST APIs
                    |
                    v
               Services
          (business logic)
                    |
                    v
       Spring Data JPA Repositories
                    |
                    v
                  MySQL

           Razorpay Checkout/API
       (server-side order/signature checks)
```

The codebase separates controllers, services, repositories, entities/models, DTOs, security components, configuration, and exception handling.

## Technology stack

| Area | Technologies |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.15, Spring MVC |
| Persistence | Spring Data JPA, Hibernate, MySQL |
| Web UI | Thymeleaf, HTML, CSS, JavaScript, Bootstrap |
| Security | Spring Security, JWT/JJWT, role-based authorization |
| Payments | Razorpay Java SDK |
| Email | Spring Mail; Gmail SMTP configuration is provided in the sample properties |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| Developer tools | Git, Postman, Swagger/OpenAPI where configured, JUnit/Spring Boot Test |

## Prerequisites

- JDK 17
- Maven (or use the Maven wrapper if one is present in your checkout)
- MySQL
- Razorpay test credentials for testing payment flows
- An email account/app password if testing email/OTP flows

## Run locally

### 1. Clone the repository

```bash
git clone https://github.com/Vikasgabale9/HourBook.git
cd HourBook
```

### 2. Create a MySQL database

Create a database, for example:

```sql
CREATE DATABASE hourbook;
```

Use a dedicated local database user where possible instead of a production/admin account.

### 3. Configure application properties

The project includes `src/main/resources/application-example.properties` as a starting point. Create your local `src/main/resources/application.properties` from that template, then set your own local values.

At minimum, configure:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `jwt.secret`
- `razorpay.key.id`
- `razorpay.key.secret`
- `spring.mail.username`
- `spring.mail.password`

Example local values (replace every placeholder; these are not real credentials):

```properties
spring.application.name=HourBook
server.port=8989

spring.datasource.url=jdbc:mysql://localhost:3306/hourbook
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

jwt.secret=REPLACE_WITH_A_LONG_RANDOM_SECRET
jwt.expiration=86400000

razorpay.key.id=YOUR_RAZORPAY_TEST_KEY_ID
razorpay.key.secret=YOUR_RAZORPAY_TEST_KEY_SECRET

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_EMAIL_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Use Razorpay test-mode credentials for local testing. Never commit your populated `application.properties` file or any `.env` file.

### 4. Build and run

```bash
mvn clean package
mvn spring-boot:run
```

Or run `HourBookApplication` from your IDE.

The default local port is **8989**, unless overridden by configuration. Open:

```text
http://localhost:8989
```

## Configuration reference

The deployed configuration uses environment variables for sensitive values. Configure the relevant variables in your hosting provider or local environment:

| Variable / property | Purpose |
|---|---|
| `DB_PASSWORD` | Database password |
| `RAZORPAY_KEY_ID` | Razorpay key ID |
| `RAZORPAY_KEY_SECRET` | Razorpay API secret |
| `RAZORPAY_WEBHOOK_SECRET` | Optional webhook secret if webhook handling is configured |
| `MAIL_USERNAME` | Email account used to send mail |
| `MAIL_PASSWORD` | Email app password/SMTP credential |
| `JWT_SECRET` | Secret used to sign JWTs |
| `PORT` | Hosting platform port override; defaults to 8989 |
| `hourbook.booking.pending-timeout-minutes` | Pending-booking expiry timeout; default in application config is 15 minutes |
| `hourbook.booking.expiry-check-ms` | Pending-booking expiry check interval; default in application config is 60000 ms |

The names above reflect the current `application.properties` placeholders and configuration. If you change property names in code, update this table too.

## Main application areas

```text
src/main/java/com/app/hourbook/
├── config/         # Security/web/file-upload configuration and admin interceptor
├── controller/     # Admin, user, vendor, ground and booking controllers
├── dto/            # Request/response data transfer objects
├── email/          # Email/OTP functionality
├── enums/          # Booking, payment, ground, KYC and role enums
├── exception/      # Custom exceptions and global exception handling
├── model/          # JPA entities/domain models
├── repository/     # Spring Data repositories
├── security/       # JWT filter and JWT service
├── service/        # Business logic
└── specification/  # Dynamic query specifications
```

Templates and static resources are under `src/main/resources/templates` and `src/main/resources/static`.

## Testing

Run the automated test suite with:

```bash
mvn test
```

The project includes Spring Boot test dependencies. For manual API checks, use Postman and test authentication, user/vendor/admin access, ground operations, booking validation, and payment verification. Use provider test credentials for payment tests.

## Important implementation notes

- Booking availability must be validated on the server; hiding a slot in the UI alone is not sufficient.
- Payment success should only be accepted after server-side signature verification.
- A production deployment should additionally be reviewed for database migrations, concurrency/race-condition protection, rate limiting, monitoring, backup/recovery, and automated integration tests.
- Uploaded KYC documents and ground images require secure storage and access controls. Do not commit runtime uploads or private documents to the repository.

## Future improvements

Potential areas for further development include automated integration tests, database migrations, stronger concurrency controls for simultaneous booking attempts, payment webhook reconciliation, cloud object storage for uploads, and CI/CD.

## License

No explicit open-source license is specified here. Unless a license is added to the repository, do not assume that others have permission to redistribute or reuse the code.
