package com.tummyfull.booking_app.dtos;

import java.time.LocalDateTime;

public class BookingDto {
    private String id;
    private String employeeId;
    private String bookingType;
    private LocalDateTime expiryTime;
    private boolean redeemed; // Include redeemed status

    // Constructors
    public BookingDto() {}

    public BookingDto(String id, String employeeId, String bookingType, LocalDateTime expiryTime, boolean redeemed) {
        this.id = id;
        this.employeeId = employeeId;
        this.bookingType = bookingType;
        this.expiryTime = expiryTime;
        this.redeemed = redeemed;
    }

    // Getters and Setters (or use Lombok for brevity)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
     public boolean isRedeemed() { return redeemed; }
    public void setRedeemed(boolean redeemed) { this.redeemed = redeemed; }
}