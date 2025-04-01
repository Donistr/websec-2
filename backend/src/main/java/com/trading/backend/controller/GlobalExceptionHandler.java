package com.trading.backend.controller;

import com.trading.backend.messages.ErrorObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorObject> handle(Exception exception) {
        return new ResponseEntity<>(new ErrorObject(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

}
