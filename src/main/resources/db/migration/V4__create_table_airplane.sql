CREATE TABLE airplane (
    id SERIAL PRIMARY KEY,
    manufacturer VARCHAR(50) NOT NULL,
    model VARCHAR(50),
    type VARCHAR(10) NOT NULL CHECK (type IN ('SMALL', 'LARGE')),
    capacity INTEGER,
    registration_number VARCHAR(20) NOT NULL UNIQUE,
    rows INTEGER NOT NULL,
    seats_per_row INT NOT NULL
);