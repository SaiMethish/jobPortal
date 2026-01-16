package com.example.jobPortal.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle404Exception(ResourceNotFoundException exception, HttpServletRequest request){
        return buildResponse(exception.getMessage(),HttpStatus.NOT_FOUND,request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException exception, HttpServletRequest request){
        return buildResponse(exception.getMessage(),HttpStatus.BAD_REQUEST,request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException exception, HttpServletRequest request){
        return buildResponse(exception.getMessage(),HttpStatus.FORBIDDEN,request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException exception,HttpServletRequest request){
        return buildResponse(exception.getMessage(),HttpStatus.UNAUTHORIZED,request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(EmailAlreadyExistsException exception, HttpServletRequest request){
        return buildResponse(exception.getMessage(),HttpStatus.CONFLICT,request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenResponse(InvalidTokenException exception, HttpServletRequest request){
        return buildResponse(exception.getMessage(), HttpStatus.UNAUTHORIZED,request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception, HttpServletRequest request){
        return buildResponse(exception.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR,request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException exception,HttpServletRequest request){
        String errorMsg=exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return buildResponse(exception.getMessage(),HttpStatus.BAD_REQUEST,request);
    }


    public ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status, HttpServletRequest request){
        ErrorResponse errorResponse=ErrorResponse.builder().
                status(status.value())
                .localDateTime(LocalDateTime.now())
                .error(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse,status);
    }
}
