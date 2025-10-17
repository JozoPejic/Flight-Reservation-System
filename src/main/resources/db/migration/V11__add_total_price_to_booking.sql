-- Add total_price column to booking table
ALTER TABLE booking ADD COLUMN total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00;