package org.example.dto;

import lombok.Data;

@Data
public class LogDto {
    private String date;
    private String time;
    private String logLevel;
    private String source;
    private String thread;
    private String message;
    private boolean isFullyFilled = false;

    public void append(String message) {
        this.message = this.message + "\n" + message;
    }
}
