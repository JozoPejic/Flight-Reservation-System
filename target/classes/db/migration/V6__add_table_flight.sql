CREATE TABLE flight(

    id SERIAL PRIMARY KEY,
    flight_number VARCHAR(10) NOT NULL,
    origin VARCHAR(50) NOT NULL,
    destination VARCHAR(50) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    airplane_id INT NOT NULL,
    CONSTRAINT fk_flight_airplane FOREIGN KEY (airplane_id)
        REFERENCES airplane(id)
        ON DELETE RESTRICT,
    CONSTRAINT unique_flight_number UNIQUE(flight_number, departure_time)
);