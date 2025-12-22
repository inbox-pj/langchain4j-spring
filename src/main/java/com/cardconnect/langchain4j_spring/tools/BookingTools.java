package com.cardconnect.langchain4j_spring.tools;

import com.cardconnect.langchain4j_spring.entity.Booking;
import com.cardconnect.langchain4j_spring.service.BookingService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Tools for AI agents to interact with the booking system.
 * These tools are exposed to CustomerSupportAgent to handle booking-related queries.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingTools {

    private final BookingService bookingService;

    /**
     * Retrieves booking details for verification.
     *
     * @param bookingNumber the booking number to look up
     * @param customerName the customer's first name
     * @param customerSurname the customer's last name
     * @return the booking details if found and customer matches
     */
    @Tool("Retrieves booking details for a given booking number and customer name. Use this when the customer asks about their booking information.")
    public Booking getBookingDetails(
            @P("bookingNumber") String bookingNumber,
            @P("customerName") String customerName,
            @P("customerSurname") String customerSurname) {
        log.debug("Tool called: getBookingDetails for booking: {}", bookingNumber);
        return bookingService.getBookingDetails(bookingNumber, customerName, customerSurname);
    }

    /**
     * Cancels an existing booking after verification.
     *
     * @param bookingNumber the booking number to cancel
     * @param customerName the customer's first name
     * @param customerSurname the customer's last name
     */
    @Tool("Cancels a booking for a given booking number and customer name. Use this ONLY after explicit customer confirmation.")
    public String cancelBooking(
            @P("bookingNumber") String bookingNumber,
            @P("customerName") String customerName,
            @P("customerSurname") String customerSurname) {
        log.info("Tool called: cancelBooking for booking: {}", bookingNumber);
        bookingService.cancelBooking(bookingNumber, customerName, customerSurname);
        return "Booking " + bookingNumber + " has been successfully cancelled. We hope to welcome you back again soon!";
    }
}