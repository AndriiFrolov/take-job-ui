package org.example.dto;

import lombok.Data;

@Data
public class ConfigurationDto {
    private Integer minTotalPrice;
    private Integer deadlineHoursFromNow;
    private Integer maxAttempts;
    private String username;
    private String password;
    private String emailToSend;
}
