ALTER TABLE flight
    ADD COLUMN IF NOT EXISTS base_price NUMERIC(10,2) NOT NULL DEFAULT 100.00;

UPDATE flight SET base_price = COALESCE(base_price, 100.00);


