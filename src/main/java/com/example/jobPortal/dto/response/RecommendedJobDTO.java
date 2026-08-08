package com.example.jobPortal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedJobDTO implements java.io.Serializable {

    private JobResponseDTO job;
    private Float score;
}
