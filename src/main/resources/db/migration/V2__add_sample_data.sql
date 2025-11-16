-- Insert a customer
INSERT INTO customer (name, surname) VALUES ('John', 'Doe');

-- Insert a booking for the customer with id 1
INSERT INTO booking (booking_number, booking_begin_date, booking_end_date, customer_id)
VALUES ('BN123', '2024-06-01', '2024-06-05', 1);
