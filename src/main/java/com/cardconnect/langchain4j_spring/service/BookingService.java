package com.cardconnect.langchain4j_spring.service;

import com.cardconnect.langchain4j_spring.entity.Booking;
import com.cardconnect.langchain4j_spring.entity.Customer;
import com.cardconnect.langchain4j_spring.exception.BookingNotFoundException;
import com.cardconnect.langchain4j_spring.repository.BookingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookingService {

    private BookingRepository bookingRepository;

    public Booking getBookingDetails(String bookingNumber, String customerName, String customerSurname) {
        ensureExists(bookingNumber, customerName, customerSurname);

        // Imitating DB lookup
        return bookingRepository.findByBookingNumber(bookingNumber);
    }

    public void cancelBooking(String bookingNumber, String customerName, String customerSurname) {
        ensureExists(bookingNumber, customerName, customerSurname);

        // Imitating booking cancellation
        bookingRepository.deleteBookingByBookingNumber(bookingNumber);
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