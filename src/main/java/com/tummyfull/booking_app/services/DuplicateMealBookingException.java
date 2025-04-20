package com.tummyfull.booking_app.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// This exception will automatically result in an HTTP 409 Conflict response
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateMealBookingException extends RuntimeException {
    public DuplicateMealBookingException(String message) {
        super(message);
    }
}