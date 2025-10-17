CREATE TABLE seat(

    id SERIAL PRIMARY KEY,
    seat_number VARCHAR(5) NOT NULL,
    seat_class VARCHAR(10) NOT NULL,
    airplane_id INTEGER NOT NULL,
    FOREIGN KEY (airplane_id)
                 REFERENCES airplane(id)
                 ON DELETE CASCADE,
    CONSTRAINT unique_seat_per_airplane UNIQUE(seat_number, airplane_id)
);