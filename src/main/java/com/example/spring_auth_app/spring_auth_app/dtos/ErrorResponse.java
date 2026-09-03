package com.example.spring_auth_app.spring_auth_app.dtos;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        String message,
        HttpStatus statusCode

) {

}
