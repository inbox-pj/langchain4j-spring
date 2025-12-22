-- Add database indexes for better query performance
-- This migration should be executed after V2

CREATE INDEX IF NOT EXISTS idx_booking_number ON booking(booking_number);
CREATE INDEX IF NOT EXISTS idx_customer_name_surname ON customer(name, surname);
CREATE INDEX IF NOT EXISTS idx_booking_customer_id ON booking(customer_id);
CREATE INDEX IF NOT EXISTS idx_chat_message_memory_id ON chat_message_entity(memory_id);

-- Add comments for documentation
COMMENT ON INDEX idx_booking_number IS 'Index for fast booking number lookups';
COMMENT ON INDEX idx_customer_name_surname IS 'Composite index for customer name searches';
COMMENT ON INDEX idx_booking_customer_id IS 'Foreign key index for booking-customer relationship';
COMMENT ON INDEX idx_chat_message_memory_id IS 'Index for chat message memory lookups';

