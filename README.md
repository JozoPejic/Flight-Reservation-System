# Flight-Reservation-System

Flight Reservation System is a web application built with Spring Boot and PostgreSQL that allows users to search for flights, reserve seats, and manage bookings.
Administrators can manage airplanes, define flights, and set seat availability with pricing based on class (Economy, Business, First).

  ## Features

* Authentication & Authorization: Register, login, and role-based access (User / Admin).

- Flight Search: Users can search flights by origin, destination, and date.

- Seat Reservation: Real-time reservation system with seat status (Available, Reserved, Booked).

- Admin Panel:

  - Add airplanes (with automatic seat generation).

  - Create and manage flights.

- Booking Management: Multi-flight itineraries with booking and booking items.

## Tech stack

* Backend: Java, Spring Boot

* Database: PostgreSQL (schema migrations with Flyway)

* ORM: Hibernate / JPA

* Security: Spring Security

* Frontend: Thymeleaf

## Database schema
Main entities:

- **User**  
  - Represents a system user who can book flights. Contains personal info like name, email, and login credentials.
  
- **Airplane**  
  - Represents an aircraft, including its model, total seats, and seat layout.

- **Seat**
  - Represents a seat on an airplane. Each seat has a class (Economy, Business, First) and a seat number.

- **Flight**
  - Represents a single flight, including origin, destination, departure and arrival times.

- **FlightSeat**
  - Links a Flight and a Seat, representing availability and booking status for that particular seat on that flight.

- **Booking**  
  - Represents a reservation made by a user. Contains booking date, total price, and references to booked items.

- **BookingItem**  
  - Represents a specific seat on a flight that is part of a booking. Contains seat class, flight info, and price.

 ## Main feature - Flight search
<div style="display: flex; gap: 10px;">

<img width="489" height="570" alt="slika" src="https://github.com/user-attachments/assets/f32f8886-71eb-4b5d-847a-c0ccbbe47d99" />
<img width="492" height="636" alt="slika" src="https://github.com/user-attachments/assets/6d903595-5ff3-4414-a015-0d8083c5ce4a" />
<img width="953" height="669" alt="slika" src="https://github.com/user-attachments/assets/9a2adae4-7415-4db7-b40b-d785240e8863" />


</div>

This is the core functionality of the application. In this example, the admin first adds two separate flights, which together can form a complete route. Later, when a user searches for a starting and destination airport, the application automatically combines these flights into a single route using a Breadth-First Search (BFS) algorithm.

This allows the system to suggest both direct and connecting flights efficiently, ensuring that users can find the best available routes with valid connections.

## How to run locally

1.) Clone the repository:
  - git clone https://github.com/your-username/flight-reservation-system.git
  - cd flight-reservation-system

2.) Configure PostgreSQL database in application.properties:
  - spring.datasource.url=jdbc:postgresql://localhost:5432/flightdb
  - spring.datasource.username=your_username
  - spring.datasource.password=your_password

3.) Run migrations with Flyway (automatically on app start).

4.) Start the app with: "./mvnw spring-boot:run"

5.) Access the API/UI at http://localhost:8082.



