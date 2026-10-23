package com.finsight.exceptions;

/** Thrown when a record does not exist, or the user is not allowed to know it exists (map to HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
