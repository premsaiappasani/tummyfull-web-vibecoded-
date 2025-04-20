package com.tummyfull.booking_app.repositories;

import com.tummyfull.booking_app.models.Booking; // Import the Booking model
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime; // Import LocalDateTime
import java.util.List; // Import List
// import java.util.Optional; // Optional is not needed here anymore, unless other methods use it


public interface BookingRepository extends MongoRepository<Booking, String> {
    // Spring Data MongoDB provides basic CRUD operations automatically
    // findById(String id), findAll(), save(Booking), deleteById(String id), etc.

    // Custom query to find bookings for a specific employee, type, within a date range
    // Used to check for existing meal bookings on a given day
    @Query("{ 'employeeId' : ?0, 'bookingType' : ?1, 'date' : { $gte: ?2, $lt: ?3 } }")
    List<Booking> findByEmployeeIdAndBookingTypeAndDateBetween(String employeeId, String bookingType, LocalDateTime startOfDay, LocalDateTime endOfNextDay);

    // Removed the findByEmail method as it belongs in EmployeeRepository
    // Optional<Employee> findByEmail(String email);

    // Count bookings by booking type within a date range
    long countByBookingTypeAndDateBetween(String bookingType, LocalDateTime startOfDay, LocalDateTime endOfNextDay);

    // Count bookings by booking type, date range, and redeemed status
    long countByBookingTypeAndDateBetweenAndRedeemed(String bookingType, LocalDateTime startOfDay, LocalDateTime endOfNextDay, boolean redeemed);

}