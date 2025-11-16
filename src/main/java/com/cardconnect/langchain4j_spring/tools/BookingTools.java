package com.cardconnect.langchain4j_spring.tools;

import com.cardconnect.langchain4j_spring.entity.Booking;
import com.cardconnect.langchain4j_spring.service.BookingService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.invocation.InvocationParameters;
import org.springframework.stereotype.Component;

@Component
public class BookingTools {

    private final BookingService bookingService;

    public BookingTools(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Tool("Retrieves booking details for a given booking number and customer name")
    public Booking getBookingDetails(InvocationParameters params) {
        String bookingNumber = params.get("bookingNumber");
        String customerName = params.get("customerName");
        String customerSurname = params.get("customerSurname");

        return bookingService.getBookingDetails(bookingNumber, customerName, customerSurname);
    }

    @Tool("Cancels a booking for a given booking number and customer name")
    public void cancelBooking(@P(value = "bookingNumber", required = true) String bookingNumber, @P(value = "customerName", required = true) String customerName, @P(value = "customerSurname", required = true) String customerSurname) {
        bookingService.cancelBooking(bookingNumber, customerName, customerSurname);
    }
}