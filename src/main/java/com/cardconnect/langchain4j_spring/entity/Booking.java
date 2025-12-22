package com.cardconnect.langchain4j_spring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Represents a customer booking in the rental system.
 * Each booking is associated with a customer and has start and end dates.
 */
@Entity
@Table(name = "booking")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_number", unique = true, nullable = false)
    private String bookingNumber;

    @Column(name = "booking_begin_date", nullable = false)
    private LocalDate bookingBeginDate;

    @Column(name = "booking_end_date", nullable = false)
    private LocalDate bookingEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", bookingNumber='" + bookingNumber + '\'' +
                ", bookingBeginDate=" + bookingBeginDate +
                ", bookingEndDate=" + bookingEndDate +
                '}';
    }
}
