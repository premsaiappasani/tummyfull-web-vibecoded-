package com.tummyfull.booking_app.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "bookings")
public class Booking {

    @Id
    private String id; // MongoDB uses String for the default _id

    private String employeeId; // To link to the Employee
    private String qrCodeLink; // Or you might store the QR code data/image itself
    private String bookingType; // e.g., "meeting", "access", "event"
    private LocalDateTime date; // Date of the booking
    private LocalDateTime expiryTime; // When the QR code expires
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean redeemed = false; // Add this new field, default to false

    public Booking() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.redeemed = false;
    }

    public Booking(String employeeId, String qrCodeLink, String bookingType, LocalDateTime date, LocalDateTime expiryTime, boolean redeemed) {
        this.employeeId = employeeId;
        this.qrCodeLink = qrCodeLink;
        this.bookingType = bookingType;
        this.date = date;
        this.expiryTime = expiryTime;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.redeemed = redeemed;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getQrCodeLink() {
        return qrCodeLink;
    }

    public void setQrCodeLink(String qrCodeLink) {
        this.qrCodeLink = qrCodeLink;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isRedeemed() { // isRedeemed is standard for boolean getters
        return redeemed;
    }

    public void setRedeemed(boolean redeemed) {
        this.redeemed = redeemed;
    }

    // You might want to add equals() and hashCode() methods
    // and a toString() method for better practice
}