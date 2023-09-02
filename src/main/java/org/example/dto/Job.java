package org.example.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import static org.example.config.Config.CURRENT_YEAR;

@Data
@EqualsAndHashCode(exclude = "appeared")
public class Job {
    private String title;
    private LocalDateTime dueDate;
    private String price;
    private String wordsCount;
    private String customer;
    private LocalDateTime appeared;


    public void setDueDateStr(String dueDateStr) {
        String inputWithYear = CURRENT_YEAR + dueDateStr;

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy EEEE, MMMM d, h:mm a zzz")
                .toFormatter();

        TemporalAccessor temporalAccessor = formatter.parse(inputWithYear);
        this.dueDate =  LocalDateTime.from(temporalAccessor);
    }
}
