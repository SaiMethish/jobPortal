package com.example.jobPortal.dto.greenhouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GreenhouseJobDTO {

    private Long id;

    private String title;

    private GreenhouseLocationDTO location;

    private String content;

    @JsonProperty("absolute_url")
    private String absoluteUrl;

    @JsonProperty("updated_at")
    private String updatedAt;
}
