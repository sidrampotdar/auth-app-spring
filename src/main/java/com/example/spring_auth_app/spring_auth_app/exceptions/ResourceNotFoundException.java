package com.example.spring_auth_app.spring_auth_app.exceptions;

public class ResourceNotFoundException extends RuntimeException {
public ResourceNotFoundException(String message) {
    super(message);
}
public ResourceNotFoundException() {
        super("Resource not found!");
    }



}
