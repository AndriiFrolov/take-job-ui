package org.example.dto;

import lombok.Data;

@Data
public class ConfigurationDto {
    private Integer minTotalPrice;
    private Integer deadlineHoursFromNow;
    private Integer maxAttempts;
    private Integer secondsBetweenRepeat;
    private String username;
    private String password;
    private String emailToSend;
}
