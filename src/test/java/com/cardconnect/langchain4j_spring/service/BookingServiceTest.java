package com.cardconnect.langchain4j_spring.service;

import com.cardconnect.langchain4j_spring.entity.Booking;
import com.cardconnect.langchain4j_spring.entity.Customer;
import com.cardconnect.langchain4j_spring.exception.BookingNotFoundException;
import com.cardconnect.langchain4j_spring.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking testBooking;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("John");
        testCustomer.setSurname("Doe");

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setBookingNumber("BN123");
        testBooking.setBookingBeginDate(LocalDate.of(2024, 6, 1));
        testBooking.setBookingEndDate(LocalDate.of(2024, 6, 5));
        testBooking.setCustomer(testCustomer);
    }

    @Test
    void shouldGetBookingDetails_whenValidBookingNumberAndCustomer() {
        // Given
        when(bookingRepository.findByBookingNumber("BN123")).thenReturn(testBooking);

        // When
        Booking result = bookingService.getBookingDetails("BN123", "John", "Doe");

        // Then
        assertNotNull(result);
        assertEquals("BN123", result.getBookingNumber());
        assertEquals("John", result.getCustomer().getName());
        assertEquals("Doe", result.getCustomer().getSurname());
        verify(bookingRepository, times(2)).findByBookingNumber("BN123");
    }

    @Test
    void shouldThrowException_whenBookingNotFound() {
        // Given
        when(bookingRepository.findByBookingNumber("INVALID")).thenReturn(null);

        // When & Then
        assertThrows(BookingNotFoundException.class, () -> {
            bookingService.getBookingDetails("INVALID", "John", "Doe");
        });

        verify(bookingRepository).findByBookingNumber("INVALID");
    }

    @Test
    void shouldThrowException_whenCustomerNameDoesNotMatch() {
        // Given
        when(bookingRepository.findByBookingNumber("BN123")).thenReturn(testBooking);

        // When & Then
        assertThrows(BookingNotFoundException.class, () -> {
            bookingService.getBookingDetails("BN123", "WrongName", "Doe");
        });
    }

    @Test
    void shouldThrowException_whenCustomerSurnameDoesNotMatch() {
        // Given
        when(bookingRepository.findByBookingNumber("BN123")).thenReturn(testBooking);

        // When & Then
        assertThrows(BookingNotFoundException.class, () -> {
            bookingService.getBookingDetails("BN123", "John", "WrongSurname");
        });
    }

    @Test
    void shouldCancelBooking_whenValidBookingAndCustomer() {
        // Given
        when(bookingRepository.findByBookingNumber("BN123")).thenReturn(testBooking);
        doNothing().when(bookingRepository).deleteBookingByBookingNumber("BN123");

        // When
        assertDoesNotThrow(() -> {
            bookingService.cancelBooking("BN123", "John", "Doe");
        });

        // Then
        verify(bookingRepository).deleteBookingByBookingNumber("BN123");
    }

    @Test
    void shouldThrowException_whenCancellingNonExistentBooking() {
        // Given
        when(bookingRepository.findByBookingNumber("INVALID")).thenReturn(null);

        // When & Then
        assertThrows(BookingNotFoundException.class, () -> {
            bookingService.cancelBooking("INVALID", "John", "Doe");
        });

        verify(bookingRepository, never()).deleteBookingByBookingNumber(anyString());
    }
}

