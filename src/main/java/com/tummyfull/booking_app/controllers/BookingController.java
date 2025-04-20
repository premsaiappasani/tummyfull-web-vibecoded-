package com.tummyfull.booking_app.controllers;

import com.tummyfull.booking_app.models.Booking;
import com.tummyfull.booking_app.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tummyfull.booking_app.dtos.VerificationResult;


import java.util.Optional;

@RestController
@RequestMapping("/api/bookings") // Base path for booking related endpoints
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Example endpoint to create a booking (without immediate QR generation)
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Booking createdBooking = bookingService.createBooking(booking);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    // New GET endpoint to find today's meal booking for an employee
    // Example: GET /api/bookings/employee/employee-id-123/today/meal
    @GetMapping("/employee/{employeeId}/today/meal")
    public ResponseEntity<Booking> getTodayMealBookingForEmployee(@PathVariable String employeeId) {
        Optional<Booking> booking = bookingService.findTodayMealBookingForEmployee(employeeId);
        return booking.map(ResponseEntity::ok) // If booking is present, return 200 OK
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build()); // Otherwise, return 404 Not Found
    }


    // Endpoint to generate and store the QR code for an existing booking
    // You might send the content to encode in the request body or build it in the service
    @PostMapping("/{bookingId}/generate-qr")
    public ResponseEntity<?> generateQrCodeForBooking(@PathVariable String bookingId,
                                                      @RequestBody QrCodeContentRequest request) {
        // Here, we assume the content for the QR code comes in the request body.
        // Alternatively, you could build the content based on the booking details in the service.
        String qrCodeContent = request.getContent(); // Get content from request body

        Optional<Booking> updatedBooking = bookingService.generateAndStoreQrCode(bookingId, qrCodeContent);

        if (updatedBooking.isPresent()) {
            // Return the updated booking object or just a success status
            return ResponseEntity.ok(updatedBooking.get());
        } else {
            // Booking not found or QR code generation failed
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found or QR code generation failed.");
        }
    }

    // Helper class for the request body of the generate-qr endpoint
    static class QrCodeContentRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    // Example endpoint to get a booking by ID (which will include the stored QR code Base64)
    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getBookingById(@PathVariable String bookingId) {
        Optional<Booking> booking = bookingService.getBookingById(bookingId);
        return booking.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // New endpoint for admin verification
    // Example: POST /api/admin/bookings/verify
    @PostMapping("/admin/verify")
    public ResponseEntity<VerificationResult> verifyBookingByEmail(@RequestBody EmailVerificationRequest request) { // Changed method name and RequestBody DTO
        String email = request.getEmail(); // Get email from the new RequestBody DTO
        VerificationResult result = bookingService.verifyMealBookingByEmail(email); // Call the new service method

        // Based on the result status, return an appropriate HTTP status code
        switch (result.getStatus()) {
            case SUCCESS:
                return ResponseEntity.ok(result); // 200 OK
            case NOT_FOUND:
                // Can differentiate employee not found vs booking not found if needed
                // The service message already provides detail
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result); // 404 Not Found
            case EXPIRED:
            case ALREADY_REDEEMED:
            case INVALID_CONTENT: // Might indicate the found booking wasn't a meal
            case EMPLOYEE_MISMATCH: // This status might not be returned by email verification, but keep for safety
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result); // 400 Bad Request for verification failures
            default:
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new VerificationResult(VerificationResult.Status.INVALID_CONTENT, "Unexpected verification error.")); // 500 Internal Server Error
        }
    }

    // Helper class for the new request body
    static class EmailVerificationRequest { // New RequestBody DTO
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // Helper class for the request body
    static class QrCodeVerificationRequest {
        private String qrContent;

        public String getQrContent() { return qrContent; }
        public void setQrContent(String qrContent) { this.qrContent = qrContent; }
    }

    // New endpoint to get total meal bookings for today
    // Example: GET /api/admin/stats/today/total
    @GetMapping("/admin/stats/today/total")
    public ResponseEntity<Long> getTotalTodayMealBookings() {
        long count = bookingService.getTotalTodayMealBookings();
        return ResponseEntity.ok(count); // Return 200 OK with the count
    }

    // New endpoint to get redeemed meal bookings for today
    // Example: GET /api/admin/stats/today/redeemed
    @GetMapping("/admin/stats/today/redeemed")
    public ResponseEntity<Long> getRedeemedTodayMealBookings() {
        long count = bookingService.getRedeemedTodayMealBookings();
        return ResponseEntity.ok(count); // Return 200 OK with the count
    }

    // ... other controller methods (update, delete, get all) ...
}