package com.cardconnect.langchain4j_spring.repository;

import com.cardconnect.langchain4j_spring.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Booking entities.
 * Provides CRUD operations and custom queries for bookings.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find a booking by its booking number.
     *
     * @param bookingNumber the unique booking number
     * @return the booking if found, null otherwise
     */
    Booking findByBookingNumber(String bookingNumber);

    /**
     * Delete a booking by its booking number.
     * This is a modifying operation that must be executed within a transaction.
     *
     * @param bookingNumber the booking number to delete
     */
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.bookingNumber = :bookingNumber")
    void deleteBookingByBookingNumber(@Param("bookingNumber") String bookingNumber);
}
