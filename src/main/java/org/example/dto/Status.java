package org.example.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class Status {
    private boolean isRunning = false;
    private Integer refreshedPageTimes = 0;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private Set<Job> foundJobs;
    private Integer takenJobs;
    private String linkToVideo;
    private String logSize;
    private List<String> errors;
}
