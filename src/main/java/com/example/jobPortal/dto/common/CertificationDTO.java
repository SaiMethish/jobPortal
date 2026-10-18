package com.example.jobPortal.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationDTO implements Serializable {
    private String name;
    private LocalDate issuedDate;
    private LocalDate expirationDate;
}
