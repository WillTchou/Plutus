package com.project.plutus.exceptions;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({EmailAlreadyExistsException.class, NotEnoughAmountException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<ExceptionResponse> handleBadRequestException(PlutusStateException exception) {
        ExceptionResponse response = ExceptionResponse.builder()
                .code(exception.getCode())
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AccountNotFoundException.class, BeneficiaryNotFoundException.class,
            TransactionNotFoundException.class, UserDoesNotExistException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseEntity<ExceptionResponse> handleNotFoundException(PlutusStateException exception) {
        ExceptionResponse response = ExceptionResponse.builder()
                .code(exception.getCode())
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<ExceptionResponse> handleException(ValidationException exception) {
        ExceptionResponse response = ExceptionResponse.builder()
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
