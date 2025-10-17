CREATE TABLE booking_item (
    id SERIAL PRIMARY KEY,
    booking_id INT NOT NULL REFERENCES booking(id) ON DELETE CASCADE,
    flight_seat_id INT NOT NULL REFERENCES flight_seat(id) ON DELETE CASCADE,
    price DECIMAL(10,2) NOT NULL
);
