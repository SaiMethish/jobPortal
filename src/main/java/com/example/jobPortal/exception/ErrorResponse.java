package com.example.jobPortal.exception;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private LocalDateTime localDateTime;
    private String error;
    private String path;
    private int status;
}
