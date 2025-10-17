-- Fix all foreign key types from INTEGER to BIGINT
ALTER TABLE seat ALTER COLUMN airplane_id TYPE BIGINT;
ALTER TABLE flight ALTER COLUMN airplane_id TYPE BIGINT;
ALTER TABLE flight_seat ALTER COLUMN flight_id TYPE BIGINT;
ALTER TABLE flight_seat ALTER COLUMN seat_id TYPE BIGINT;
ALTER TABLE booking ALTER COLUMN user_id TYPE BIGINT;
ALTER TABLE booking_item ALTER COLUMN booking_id TYPE BIGINT;
ALTER TABLE booking_item ALTER COLUMN flight_seat_id TYPE BIGINT;
