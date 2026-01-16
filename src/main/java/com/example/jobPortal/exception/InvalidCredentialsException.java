package com.example.jobPortal.exception;

public class InvalidCredentialsException extends RuntimeException{
    public  InvalidCredentialsException(String message){
        super(message);
    }
}
