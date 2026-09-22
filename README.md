# HourBook – Sports Ground Booking Platform

HourBook is a full-stack sports ground booking platform built with Java, Spring Boot, Spring Security, Thymeleaf, JPA/Hibernate, MySQL and Razorpay.

The platform provides separate workflows for Users, Vendors and Admins. Users can discover sports grounds and book available time slots, vendors can manage their grounds and monitor bookings/revenue, and administrators can manage users, vendors, grounds and approvals.

---

## 🚀 Project Overview

HourBook solves the problem of manually managing sports-ground availability and bookings.

The system provides:

- Sports ground discovery and search
- Ground details, images, amenities and operating hours
- Hourly slot generation
- Real-time-style slot availability based on existing bookings
- User registration and OTP verification
- Vendor registration and OTP verification
- Vendor KYC management
- Admin approval workflow
- Ground approval/rejection
- Secure booking workflow
- Razorpay payment integration
- Server-side payment signature verification
- Booking history
- Booking cancellation
- Vendor booking management
- Vendor revenue tracking
- Role-based access control

---

## ✨ Main Features

### 👤 User

- User registration and login
- Email/phone verification using OTP
- User dashboard
- Browse available sports grounds
- Search grounds by name
- View ground details
- View ground images
- View sports type and ground type
- View amenities
- View price per hour
- View opening and closing time
- Check available booking slots
- Select booking date and time
- Create a booking
- Razorpay checkout
- Server-side payment verification
- View booking history
- View booking status
- Cancel bookings
- User profile management

### 🏢 Vendor

- Vendor registration and login
- OTP verification
- Vendor profile
- KYC submission
- Aadhaar/PAN document upload
- Profile image upload
- KYC status
- Add sports grounds
- Edit grounds
- Enable/disable grounds
- View vendor grounds
- Configure: Sport, Ground type, Location, Price per hour, Opening time, Closing time, Slot duration, Amenities, Ground images
- View bookings for vendor-owned grounds
- Filter bookings by status (confirmed / pending / cancelled)
- Revenue dashboard
- View paid/confirmed booking revenue

### 🛡️ Admin

- Admin login
- Admin dashboard
- View total users, vendors, grounds and bookings
- Ground approval status dashboard
- Manage users (activate/deactivate)
- Manage vendors
- Review vendor KYC and documents, approve/reject KYC
- Manage grounds (approve/reject, enable/disable, view details, add ground)
- Manage bookings
- Admin session protection

---

## 🏗️ Architecture

HourBook follows a layered Spring Boot architecture.

```
                    ┌───────────────────────┐
                    │       Browser         │
                    │  HTML / CSS / JS      │
                    │     Thymeleaf         │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │      Controller       │
                    │  Web + REST APIs      │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │       Service         │
                    │ Business Logic        │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │      Repository       │
                    │ Spring Data JPA       │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │        MySQL          │
                    └───────────────────────┘

                         Payment Flow
                              │
                              ▼
                    ┌───────────────────────┐
                    │       Razorpay        │
                    │  Checkout + Orders    │
                    └───────────────────────┘
```

---

## 🔐 Authentication & Security

HourBook uses two authentication approaches because the application contains both REST APIs and server-rendered web pages.

### REST API Authentication

The `/api/**` endpoints use:

- JWT
- Spring Security
- JWT authentication filter
- Role-based authorization

Typical roles:

```
ROLE_USER
ROLE_VENDOR
```

### Web Application Authentication

The Thymeleaf dashboards use HTTP session-based authentication.

Examples:

```
/user/**
/vendor/**
/admin/**
```

Admin routes are additionally protected using an `AdminInterceptor`.

JWT is not used for every page in the application. The project intentionally uses JWT for REST APIs and sessions for the server-rendered dashboards.

---

## 💳 Razorpay Payment Flow

HourBook integrates Razorpay for online booking payments.

The payment workflow is:

```
User selects ground
        ↓
Select date and time slot
        ↓
Server validates slot
        ↓
Booking created as PENDING
        ↓
Razorpay Order created
        ↓
Razorpay Checkout opens
        ↓
User completes payment
        ↓
Razorpay returns:
    - Order ID
    - Payment ID
    - Signature
        ↓
Server verifies HMAC-SHA256 signature
        ↓
Payment verified
        ↓
PaymentStatus = PAID
        ↓
BookingStatus = CONFIRMED
```

### Payment Security

The server verifies the Razorpay signature using:

```
razorpayOrderId + "|" + razorpayPaymentId
```

The signature is verified using HMAC-SHA256 with the Razorpay secret.

The booking is confirmed only after successful server-side verification.

> **Note:** Existing bookings created before `PaymentStatus.PAID` was persisted may display a missing/unknown payment status. New successful payments persist both `PaymentStatus = PAID` and `BookingStatus = CONFIRMED`.

### Payment Status

```
CREATED
PENDING
PAID
FAILED
REFUNDED
```

### Booking Status

```
PENDING
CONFIRMED
CANCELLED
COMPLETED
```

---

## 📅 Booking & Slot Management

The application generates hourly slots based on the ground's configuration.

For example:

```
Opening Time : 06:00 AM
Closing Time : 11:00 PM
Slot Duration: 60 minutes
```

Generated slots include:

```
06:00 - 07:00
07:00 - 08:00
08:00 - 09:00
...
10:00 - 11:00
```

The server prevents already-booked slots from being booked again. Bookings with these statuses block the slot:

```
PENDING
CONFIRMED
```

For the current date, past time slots are also disabled.

---

## 🗄️ Database

The application uses MySQL with Spring Data JPA / Hibernate.

Main domain entities include:

```
AppUserDetails
Vendor
Ground
Booking
```

The booking entity contains Razorpay payment information directly:

```
razorpayOrderId
razorpayPaymentId
razorpaySignature
paymentStatus
```

There is no separate Payment entity in the current implementation.

---

## 📦 Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 3.5.15 | Backend framework |
| Spring MVC | Web layer |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| MySQL | Database |
| Thymeleaf | Server-side UI |
| HTML5 | Frontend |
| CSS3 | Styling |
| JavaScript | Client-side functionality |
| Bootstrap | UI components |
| JWT / JJWT 0.12.6 | REST API authentication |
| Razorpay Java SDK 1.4.10 | Online payments |
| Spring Mail | Email/OTP |
| Jakarta Validation | Request validation |
| Lombok | Boilerplate reduction |
| Maven | Build and dependency management |
| Postman | API testing |
| Git/GitHub | Version control |

---

## 📁 Project Structure

```
HourBook/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/app/hourbook/
│   │   │       ├── HourBookApplication.java
│   │   │       ├── config/            # AdminInterceptor, FileUploadConfig, SecurityConfig, WebConfig
│   │   │       ├── controller/
│   │   │       │   ├── admin/         # AdminMvcController, AdminRegisterController
│   │   │       │   ├── ground/        # GroundController (admin ground approval/CRUD)
│   │   │       │   ├── user/          # UserBookingController, UserDashboardController, UserGroundController, UserLoginController, UserProfileController, AppUserController, AppUserDetailsController
│   │   │       │   └── vendor/        # VendorController, VendorDashBoardController, VendorDetailsController, VendorKycMvcController, VendorLoginController, VendorPanelGroundController, VendorProfileController, VendorStaticController
│   │   │       ├── dto/
│   │   │       │   ├── admin/
│   │   │       │   ├── booking/
│   │   │       │   └── user/
│   │   │       ├── email/             # EmailService (OTP / mail)
│   │   │       ├── enums/             # Amenities, BookingStatus, GroundStatus, GroundType, KycStatus, PaymentStatus, Role
│   │   │       ├── exception/         # GlobalExceptionHandler + custom exceptions
│   │   │       ├── model/             # AppAdmin, AppUser, AppUserDetails, Booking, Ground, Vendor, VendorDetails
│   │   │       ├── repository/        # Spring Data JPA repositories
│   │   │       ├── security/          # JwtAuthenticationFilter, JwtService, AuthenticatedPrincipal
│   │   │       ├── service/           # business logic + *ServiceImpl classes
│   │   │       └── specification/     # GroundSpecification, UserSpecification, VendorSpecification
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/admin.css
│   │       │   └── favicon.ico
│   │       ├── templates/
│   │       │   ├── admin/             # dashboard, grounds, users, vendors, add/edit-ground, fragments/
│   │       │   ├── user/              # dashboard, grounds, ground-details, book-slot, booking, payment, profile, fragments/
│   │       │   └── vendor/            # dashboard, grounds, bookings, revenue, kyc, profile, fragments/
│   │       ├── application.properties        # not committed (see .gitignore)
│   │       └── application-example.properties # sample/template config
│   │
│   └── test/
│       └── java/com/app/hourbook/HourBookApplicationTests.java
│
├── screenshots/
├── uploads/            # runtime file storage (ground images, vendor KYC docs) — not part of source
├── pom.xml
├── .gitignore
└── README.md
```

> Note: `AdminInterceptor` (route protection for `/admin/**`) lives under `config/`, and the JWT filter (`JwtAuthenticationFilter`) lives under `security/` — there are no separate top-level `filter/` or `interceptor/` packages in the actual codebase.

---

## 🔌 Important Web Routes

### User

```
GET  /user/dashboard
GET  /user/grounds
GET  /user/grounds/{groundId}
GET  /user/bookings
GET  /user/bookings/ground/{groundId}
POST /user/bookings/create
POST /user/bookings/verify-payment
POST /user/bookings/cancel/{bookingId}
```

### Vendor

```
GET /vendor/dashboard
GET /vendor/profile
GET /vendor/grounds
GET /vendor/bookings
GET /vendor/revenue
GET /vendor/kyc
```

Vendor booking filtering:

```
/vendor/bookings?status=CONFIRMED
/vendor/bookings?status=PENDING
/vendor/bookings?status=CANCELLED
```

### Admin

```
GET /admin/login
GET /admin/dashboard
GET /admin/users
GET /admin/vendors
GET /admin/grounds
GET /admin/grounds/add
GET /admin/bookings
```

---

## 💰 Vendor Revenue

Vendor revenue is calculated from confirmed bookings.

Current business rule:

```
Revenue = SUM(totalAmount)
          for CONFIRMED bookings
```

Example:

```
Booking #1 → ₹1
Booking #2 → ₹5,000
Booking #3 → ₹1
Total Revenue = ₹5,002
```

Only confirmed bookings are included in the vendor revenue calculation.

---

## 🧑‍💼 Admin Ground Approval

A vendor-created ground does not automatically become bookable.

```
Vendor creates ground
        ↓
Ground status = PENDING
        ↓
Admin reviews ground
        ↓
       ┌───────────────┐
       │               │
       ▼               ▼
   APPROVED          REJECTED
       │
       ▼
Ground can be
made available
for booking
```

A user can book only an approved and active ground.

---

## 🪪 Vendor KYC

Vendors can submit KYC information including:

- Full name
- Aadhaar number
- PAN number
- Date of birth
- Profile image
- Aadhaar document
- PAN document

Admin can review the submitted KYC documents and manage the KYC status.

> For a production application, sensitive identity information should be masked, encrypted where appropriate, access-controlled and handled according to applicable privacy and data-protection requirements.

---

## 🖼️ Application Screens

The application contains separate interfaces for Admin, Vendor and User.

### Admin

**Admin Login**
![Admin Login](screenshots/adminLogin.png)

**Admin Dashboard**
![Admin Dashboard](screenshots/adminDashboard.png)

**User Management**
![User Management](screenshots/adminUserManagement.png)

**Vendor Management**
![Vendor Management](screenshots/adminVendorManagement.png)

**Ground Management**
![Ground Management](screenshots/adminViewGrounds.png)

**Add Ground – Form**
![Admin Add Ground](screenshots/adminAddGround1.png)

**Add Ground – Details**
![Admin Add Ground Details](screenshots/adminAddGround2.png)

**Ground Details**
![Ground Details](screenshots/ground1.png)

**KYC / Document Review**
![Document Viewer](screenshots/documentViewer.png)

### Vendor

**Vendor Dashboard**
![Vendor Dashboard](screenshots/VendorDashboard.png)

**Vendor Grounds**
![Vendor Grounds](screenshots/VendorGrounds.png)

**Vendor Bookings**
![Vendor Bookings](screenshots/VendorBookings.png)

**Vendor Booking Details**
![Vendor Booking Details](screenshots/vendorBookingDetails.png)

**Vendor Booking Management**
![Vendor Booking Dashboard](screenshots/VendorBookingDashboard.png)

**Vendor Profile**
![Vendor Profile](screenshots/vendorProfile.png)

**Vendor Profile / Bookings**
![Vendor Profile Bookings](screenshots/VendorProfileBookings.png)

**Vendor KYC**
![Vendor KYC](screenshots/VendorKYC.png)

**Vendor Revenue**
![Vendor Revenue](screenshots/vendorRevenue.png)

### User

**User Dashboard**
![User Dashboard](screenshots/UserDashboard.png)

**Browse Grounds**
![User Browse Grounds](screenshots/userBrowseGrounds.png)

**Ground Listing**
![User View Grounds](screenshots/UserViewGrounds.png)

**Ground Details**
![User Ground Details](screenshots/ground1.png)

**Book Ground**
![User Book Ground](screenshots/userBookGround.png)

**Select Booking Slot**
![User Book Slot](screenshots/userBookSlot.png)

**Booking Slot – Details**
![User Book Slot Details](screenshots/userBookSlot2.png)

**Booking Summary**
![User Booking](screenshots/userBooking.png)

**Razorpay Order Creation**
![Razorpay Order Creation](screenshots/razorpayOrderCreation.png)

**Payment Processing**
![Payment Processing](screenshots/paymentProcessing.png)

**Payment Successful**
![Payment Successful](screenshots/paymentSuccessfull.png)

**My Bookings**
![User Bookings](screenshots/UserBookings.png)

**User Profile**
![User Profile](screenshots/userProfile.png)

---

## ⚙️ Setup & Installation

### 1. Clone the Repository

```
git clone https://github.com/Vikasgabale9/HourBook.git
cd HourBook
```

### 2. Requirements

Install the following:

- Java 17+
- Maven 3.8+
- MySQL 8+
- Git
- IDE such as IntelliJ IDEA / Eclipse / Spring Tool Suite
- Postman (optional, for API testing)

### 3. Create MySQL Database

```sql
CREATE DATABASE hourbook;
```

### 4. Configure Application Properties

Create/configure `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hourbook
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
server.port=8989

# Razorpay
razorpay.key.id=YOUR_RAZORPAY_KEY_ID
razorpay.key.secret=YOUR_RAZORPAY_KEY_SECRET

# Mail
spring.mail.host=YOUR_SMTP_HOST
spring.mail.port=YOUR_SMTP_PORT
spring.mail.username=YOUR_EMAIL
spring.mail.password=YOUR_EMAIL_APP_PASSWORD

# JWT
jwt.secret=YOUR_JWT_SECRET
```

> Do not commit real passwords, API keys, SMTP passwords or JWT secrets to GitHub. The project's `.gitignore` should exclude `application.properties`. For production, use environment variables or a secure secret-management solution.

### ▶️ Run the Application

Using Maven:

```
mvn clean install
mvn spring-boot:run
```

Or run the main Spring Boot application class from your IDE.

The application runs on: **http://localhost:8989**

---

## 🧪 Testing

Postman can be used to test REST APIs. Test areas include:

- Authentication
- User APIs
- Vendor APIs
- Ground APIs
- Booking APIs
- Payment APIs

For Razorpay testing, use Razorpay test credentials and test-mode payments.

---

## 🔄 Complete User Booking Flow

```
1. User logs in
        ↓
2. User opens Browse Grounds
        ↓
3. User selects a ground
        ↓
4. User opens Ground Details
        ↓
5. User selects booking date
        ↓
6. Server generates available slots
        ↓
7. User selects a slot
        ↓
8. Server validates the ground
        ↓
9. Server validates date/time
        ↓
10. Server checks slot availability
        ↓
11. Booking created as PENDING
        ↓
12. Razorpay order created
        ↓
13. Razorpay Checkout opens
        ↓
14. User completes payment
        ↓
15. Razorpay returns payment details
        ↓
16. Server verifies signature
        ↓
17. PaymentStatus = PAID
        ↓
18. BookingStatus = CONFIRMED
        ↓
19. User sees booking in My Bookings
        ↓
20. Vendor sees booking in Vendor Bookings
        ↓
21. Confirmed amount contributes to revenue
```

---

## 🛡️ Important Booking Validations

The backend validates:

- User exists
- Ground exists
- Ground is approved
- Ground is active
- Booking date is valid
- Booking time is within operating hours
- Slot follows configured slot duration
- Slot is not already booked
- User owns the booking during payment verification
- Razorpay order ID matches the stored order
- Razorpay signature is valid
- Cancelled/completed bookings cannot be incorrectly reused

Business logic is handled on the server rather than trusting client-side values.

---

## 🧩 Design Highlights

### Layered Architecture

```
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

This keeps request handling, business logic and persistence responsibilities separated.

### DTO-Based API Design

DTOs are used where appropriate to transfer data between API/UI layers without exposing unnecessary entity details.

### JPA Specifications

JPA Specifications are used for dynamic/filter-based database queries where applicable.

### Role-Based Access

Different application areas are separated according to:

```
USER
VENDOR
ADMIN
```

### Payment Verification

Payment confirmation is performed on the server instead of trusting only the browser response.

---

## 📊 Current Application Modules

```
                    HourBook
                       │
        ┌──────────────┼──────────────┐
        │              │              │
       User          Vendor          Admin
        │              │              │
        ▼              ▼              ▼
   Browse Grounds   Manage Grounds  Manage Users
   Book Slots       Manage KYC      Manage Vendors
   Payments         Bookings        Approve Grounds
   My Bookings      Revenue         Manage Bookings
   Profile          Profile         Dashboard
```

---

## 🔮 Future Enhancements

Possible improvements for a production-ready version:

- Redis-based slot locking
- Scheduled cleanup of abandoned PENDING bookings
- Automatic refund workflow
- Razorpay webhooks
- Email booking confirmation
- SMS notifications
- Vendor earnings reports
- Monthly revenue charts
- Advanced ground search/filtering
- Location/map integration
- Reviews and ratings
- Coupon/discount system
- Admin analytics
- Pagination for large booking datasets
- Cloud storage such as AWS S3 for images/documents
- Docker deployment
- CI/CD pipeline
- Automated unit and integration tests
- Production monitoring and logging

---

## 🧑‍💻 Developer

**Vikas Gabale**
Java Backend / Spring Boot Developer

**GitHub:**
[https://github.com/Vikasgabale9](https://github.com/Vikasgabale9)

**Project:**
[HourBook – Sports Ground Booking Platform](https://github.com/Vikasgabale9/HourBook)

---

## 📄 License

This project is intended for learning, portfolio and demonstration purposes.

Add an appropriate open-source license before distributing the project publicly if required.