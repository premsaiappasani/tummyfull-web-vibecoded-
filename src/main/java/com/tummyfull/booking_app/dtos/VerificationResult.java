package com.tummyfull.booking_app.dtos;

public class VerificationResult {

    public enum Status {
        SUCCESS, EXPIRED, NOT_FOUND, ALREADY_REDEEMED, INVALID_CONTENT, EMPLOYEE_MISMATCH
    }

    private Status status;
    private String message;
    private BookingDto bookingDetails; // Optional: Can include details of the verified booking

    public VerificationResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public VerificationResult(Status status, String message, BookingDto bookingDetails) {
        this.status = status;
        this.message = message;
        this.bookingDetails = bookingDetails;
    }


    // Getters
    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public BookingDto getBookingDetails() {
        return bookingDetails;
    }
}