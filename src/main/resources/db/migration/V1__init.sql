CREATE TABLE CHAT_MESSAGE_ENTITY
(
    MEMORY_ID BIGINT      NOT NULL,
    CONTENT   LONGVARCHAR NOT NULL
);

-- Customer table
CREATE TABLE customer
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(255),
    surname VARCHAR(255)
);

-- Booking table
CREATE TABLE booking
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_number     VARCHAR(255),
    booking_begin_date DATE,
    booking_end_date   DATE,
    customer_id        BIGINT,
    FOREIGN KEY (customer_id) REFERENCES customer (id)
);
