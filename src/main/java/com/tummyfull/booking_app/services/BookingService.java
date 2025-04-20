package com.tummyfull.booking_app.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.tummyfull.booking_app.models.Booking;
import com.tummyfull.booking_app.repositories.BookingRepository;
import com.tummyfull.booking_app.models.Employee;
import com.tummyfull.booking_app.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import com.tummyfull.booking_app.dtos.BookingDto;
import com.tummyfull.booking_app.dtos.VerificationResult;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Method to generate QR code and store Base64 in booking
    public Optional<Booking> generateAndStoreQrCode(String bookingId, String qrCodeContent) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);

        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            try {
                // Generate QR Code image
                BufferedImage qrCodeImage = generateQrCodeImage(qrCodeContent, 200, 200); // Specify width and height

                // Convert BufferedImage to Base64 String
                String qrCodeBase64 = convertImageToBase64(qrCodeImage, "PNG");

                // Store the Base64 string in the booking
                booking.setQrCodeLink(qrCodeBase64);
                Booking updatedBooking = bookingRepository.save(booking); // Save the updated booking

                return Optional.of(updatedBooking);

            } catch (Exception e) {
                // Log the exception (you should use a proper logger)
                e.printStackTrace();
                // Handle the error appropriately
                return Optional.empty();
            }
        } else {
            // Booking not found
            return Optional.empty();
        }
    }

    // Helper method to generate QR code image
    private BufferedImage generateQrCodeImage(String content, int width, int height) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Error correction level
        hints.put(EncodeHintType.MARGIN, 1); // QR Code margins

        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    // Helper method to convert BufferedImage to Base64 String
    private String convertImageToBase64(BufferedImage image, String format) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    // You might also want methods to get bookings, create bookings without QR, etc.
    // For example:
    public Optional<Booking> getBookingById(String id) {
        return bookingRepository.findById(id);
    }

    public Optional<Booking> findTodayMealBookingForEmployee(String employeeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfNextDay = today.plusDays(1).atStartOfDay(); // Until start of tomorrow

        List<Booking> bookings = bookingRepository.findByEmployeeIdAndBookingTypeAndDateBetween(
                employeeId, "meal", startOfDay, endOfNextDay
        );

        // Since we enforce one meal per day, the list should have at most one element
        if (bookings.isEmpty()) {
            return Optional.empty(); // No meal booking found for today
        } else {
            return Optional.of(bookings.get(0)); // Return the first (and should be only) booking
        }
    }

    public Booking createBooking(Booking booking) {
        // Set creation and update timestamps
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        // --- Implement Meal Booking Rules ---
        if ("meal".equalsIgnoreCase(booking.getBookingType())) { // Case-insensitive check for "meal" type
            // Rule 1: Check for existing meal booking today

            // Get the start and end of the day for the booking date
            LocalDate bookingDate = booking.getDate().toLocalDate();
            LocalDateTime startOfDay = bookingDate.atStartOfDay(); // Start of the booking day (00:00)
            // End of the day is start of next day
            LocalDateTime endOfNextDay = bookingDate.plusDays(1).atStartOfDay();

            List<Booking> existingMealBookings = bookingRepository.findByEmployeeIdAndBookingTypeAndDateBetween(
                booking.getEmployeeId(), "meal", startOfDay, endOfNextDay
            );

            if (!existingMealBookings.isEmpty()) {
                // A meal booking already exists for this employee today
                throw new com.tummyfull.booking_app.services.DuplicateMealBookingException("You have already booked a meal for today.");
            }

            // Rule 2: Set expiry time to the end of the booking day
            // We already calculated the end of day based on the bookingDate above
            // Set expiry to 23:59:59.999 of the booking date
            booking.setExpiryTime(bookingDate.atTime(LocalTime.MAX)); // LocalTime.MAX is 23:59:59.999999999
             // Note: Using LocalTime.MAX is precise. If you need exactly 23:59:59,
             // you could use .atTime(23, 59, 59) instead.

            // The date field should represent the day of the booking, so using the
            // LocalDate from above and setting time to start of day might be appropriate
            // depending on how you want to interpret the 'date' field.
            // If 'date' includes the time they booked, keep it as is.
            // If 'date' should just be the day, you might set it like this:
            // booking.setDate(startOfDay); // Example: Set time to 00:00:00
            // For now, we'll assume the 'date' field might include time and just enforce expiry.

        }
        // --- End of Meal Booking Rules ---


        return bookingRepository.save(booking); // Save the booking
    }

    // Method to parse QR content string
    private Map<String, String> parseQrContent(String qrContent) {
        Map<String, String> params = new HashMap<>();
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return params; // Return empty map for null or empty input
        }
        // Expected format: key1=value1_&_key2=value2...
        // We need to handle potential URL encoding if the content is more complex
        // For the simple format email=_&_bookingId=_&_employeeId=, we can split by _&_
        String[] pairs = qrContent.split("_&_");
        Pattern pattern = Pattern.compile("([^=]+)=(.+)"); // Regex to capture key=value

        for (String pair : pairs) {
            Matcher matcher = pattern.matcher(pair);
            if (matcher.matches()) {
                // Decode URL encoded values if necessary (though for simple IDs, maybe not)
                // String key = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8);
                // String value = URLDecoder.decode(matcher.group(2), StandardCharsets.UTF_8);
                String key = matcher.group(1);
                String value = matcher.group(2);
                params.put(key, value);
            }
        }
        return params;
    }


    // New method to verify a booking from QR content
    public VerificationResult verifyBooking(String qrContent) {
        Map<String, String> qrData = parseQrContent(qrContent);

        String bookingId = qrData.get("bookingId");
        String employeeIdFromQR = qrData.get("employeeId"); // Get employeeId from QR too
        // String emailFromQR = qrData.get("email"); // Get email from QR too

        if (bookingId == null || bookingId.trim().isEmpty() || employeeIdFromQR == null || employeeIdFromQR.trim().isEmpty()) {
            return new VerificationResult(VerificationResult.Status.INVALID_CONTENT, "Invalid QR code content. Missing bookingId or employeeId.");
        }

        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);

        if (!optionalBooking.isPresent()) {
            return new VerificationResult(VerificationResult.Status.NOT_FOUND, "Booking not found.");
        }

        Booking booking = optionalBooking.get();

        // Optional: Verify if employeeId from QR matches the employeeId in the booking
        if (!booking.getEmployeeId().equals(employeeIdFromQR)) {
            // This could indicate a forged QR code attempting to use a different booking ID
            return new VerificationResult(VerificationResult.Status.EMPLOYEE_MISMATCH, "QR code does not match booking details.");
        }


        // Check if expired
        if (LocalDateTime.now().isAfter(booking.getExpiryTime())) {
            return new VerificationResult(VerificationResult.Status.EXPIRED, "Booking has expired.");
        }

        // Check if already redeemed
        if (booking.isRedeemed()) {
            // You might still return the booking details even if already redeemed
            BookingDto bookingDto = new BookingDto(
                    booking.getId(),
                    booking.getEmployeeId(),
                    booking.getBookingType(),
                    booking.getExpiryTime(),
                    booking.isRedeemed() // Will be true
            );
            return new VerificationResult(VerificationResult.Status.ALREADY_REDEEMED, "Booking has already been redeemed.", bookingDto);
        }

        // If we reach here, the booking is valid and not redeemed. Mark as redeemed.
        booking.setRedeemed(true);
        booking.setUpdatedAt(LocalDateTime.now()); // Update timestamp
        Booking updatedBooking = bookingRepository.save(booking); // Save the updated booking

        // Return success result with updated booking details
        BookingDto bookingDto = new BookingDto(
                updatedBooking.getId(),
                updatedBooking.getEmployeeId(),
                updatedBooking.getBookingType(),
                updatedBooking.getExpiryTime(),
                updatedBooking.isRedeemed() // Will be true
        );

        return new VerificationResult(VerificationResult.Status.SUCCESS, "Booking successfully redeemed.", bookingDto);
    }

    // New method to get the total count of meal bookings for today
    public long getTotalTodayMealBookings() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfNextDay = today.plusDays(1).atStartOfDay();

        return bookingRepository.countByBookingTypeAndDateBetween("meal", startOfDay, endOfNextDay);
    }

    // New method to get the count of redeemed meal bookings for today
    public long getRedeemedTodayMealBookings() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfNextDay = today.plusDays(1).atStartOfDay();

        return bookingRepository.countByBookingTypeAndDateBetweenAndRedeemed("meal", startOfDay, endOfNextDay, true);
    }

    public VerificationResult verifyMealBookingByEmail(String email) {

        // 1. Find the Employee by email
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);

        if (!optionalEmployee.isPresent()) {
            return new VerificationResult(VerificationResult.Status.NOT_FOUND, "Employee not found with this email.");
        }

        Employee employee = optionalEmployee.get();
        String employeeId = employee.getId();


        // 2. Find today's meal booking for this employee
        // Reuse the existing findTodayMealBookingForEmployee method
        Optional<Booking> optionalBooking = findTodayMealBookingForEmployee(employeeId);


        if (!optionalBooking.isPresent()) {
            return new VerificationResult(VerificationResult.Status.NOT_FOUND, "No meal booking found for this employee today.");
        }

        Booking booking = optionalBooking.get();

        // 3. Perform verification checks on the booking
        // Check if expired
        if (LocalDateTime.now().isAfter(booking.getExpiryTime())) {
            // You might want a more specific status here like BOOKING_EXPIRED vs NOT_FOUND
            // But for simplicity, let's use EXPIRED
            return new VerificationResult(VerificationResult.Status.EXPIRED, "Booking has expired.");
        }

        // Check if already redeemed
        if (booking.isRedeemed()) {
             BookingDto bookingDto = new BookingDto(
                 booking.getId(),
                 booking.getEmployeeId(),
                 booking.getBookingType(),
                 booking.getExpiryTime(),
                 booking.isRedeemed()
             );
            return new VerificationResult(VerificationResult.Status.ALREADY_REDEEMED, "Booking has already been redeemed.", bookingDto);
        }

        // If we reach here, the booking is valid and not redeemed. Mark as redeemed.
        // Ensure it's a meal booking just in case (though findTodayMealBookingForEmployee already checks type)
        if (!"meal".equalsIgnoreCase(booking.getBookingType())) {
             // Should not happen if findTodayMealBookingForEmployee is correct, but as a safeguard
             return new VerificationResult(VerificationResult.Status.INVALID_CONTENT, "Booking found is not a meal booking.");
        }


        booking.setRedeemed(true);
        booking.setUpdatedAt(LocalDateTime.now()); // Update timestamp
        Booking updatedBooking = bookingRepository.save(booking); // Save the updated booking

        // Return success result with updated booking details
        BookingDto bookingDto = new BookingDto(
            updatedBooking.getId(),
            updatedBooking.getEmployeeId(),
            updatedBooking.getBookingType(),
            updatedBooking.getExpiryTime(),
            updatedBooking.isRedeemed()
        );

        return new VerificationResult(VerificationResult.Status.SUCCESS, "Booking successfully redeemed.", bookingDto);
    }

    // ... other service methods ...
}