package com.cardconnect.langchain4j_spring.service;

import com.cardconnect.langchain4j_spring.entity.Booking;
import com.cardconnect.langchain4j_spring.entity.Customer;
import com.cardconnect.langchain4j_spring.exception.BookingNotFoundException;
import com.cardconnect.langchain4j_spring.repository.BookingRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class BookingService {

    private BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public Booking getBookingDetails(String bookingNumber, String customerName, String customerSurname) {
        log.debug("Fetching booking details for booking number: {}", bookingNumber);
        ensureExists(bookingNumber, customerName, customerSurname);

        // Imitating DB lookup
        return bookingRepository.findByBookingNumber(bookingNumber);
    }

    @Transactional
    public void cancelBooking(String bookingNumber, String customerName, String customerSurname) {
        log.info("Attempting to cancel booking: {}", bookingNumber);
        ensureExists(bookingNumber, customerName, customerSurname);

        // Imitating booking cancellation
        bookingRepository.deleteBookingByBookingNumber(bookingNumber);
        log.info("Successfully cancelled booking: {}", bookingNumber);
    }

    private void ensureExists(String bookingNumber, String customerName, String customerSurname) {
        // Imitating DB lookup

        Booking booking = bookingRepository.findByBookingNumber(bookingNumber);
        if (booking == null) {
            throw new BookingNotFoundException(bookingNumber);
        }

        Customer customer = booking.getCustomer();
        if (!customer.getName().equals(customerName)) {
            throw new BookingNotFoundException(bookingNumber);
        }
        if (!customer.getSurname().equals(customerSurname)) {
            throw new BookingNotFoundException(bookingNumber);
        }
    }
}