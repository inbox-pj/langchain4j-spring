package com.cardconnect.langchain4j_spring.repository;

import com.cardconnect.langchain4j_spring.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Booking findByBookingNumber(String bookingNumber);

    void deleteBookingByBookingNumber(String bookingNumber);
}
