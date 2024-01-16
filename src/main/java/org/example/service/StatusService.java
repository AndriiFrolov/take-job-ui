package org.example.service;

import org.example.dto.Job;
import org.example.dto.Status;
import org.openqa.selenium.logging.Logs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Service
public class StatusService {

    private boolean shouldRun;
    private boolean isRunning;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private Integer refreshedPageTimes = 0;

    private Integer takenJobs = 0;
    private Set<Job> jobsFound = new HashSet<>();

    @Autowired
    private LogsService logsService;

    public void start() {
        shouldRun = true;
        isRunning = true;
        startedAt = LocalDateTime.now();
        takenJobs = 0;
        refreshedPageTimes = 0;
        jobsFound = new HashSet<>();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isShouldRun() {
        return shouldRun;
    }

    public void markShouldNotRunAnymore() {
        shouldRun = false;
    }

    public void stop() {
        shouldRun = false;
        stoppedAt = LocalDateTime.now();
    }

    public void tookAJob() {
        this.takenJobs += 1;
    }

    public void refreshedPage() {
        this.refreshedPageTimes += 1;
    }

    public Integer getTakenJobs() {
        return takenJobs;
    }

    public Status getStatus() {
        Status status = new Status();
        status.setFoundJobs(jobsFound);
        status.setRunning(isRunning);
        status.setStartedAt(startedAt);
        status.setStoppedAt(stoppedAt);
        status.setRefreshedPageTimes(refreshedPageTimes);
        status.setTakenJobs(takenJobs);
        status.setLogSize(logsService.getFolderSizeStr(new File("logs")));

        String seleniumDriverHost = System.getenv().getOrDefault("SELENIUM_GRID_HOST", "localhost");
        status.setLinkToVideo(String.format("http://%s:7900/?autoconnect=1&resize=scale&password=secret", seleniumDriverHost));
        return status;
    }

    public void addFoundJob(Job job) {
        this.jobsFound.add(job);
        this.printJobTable(this.jobsFound);
    }

    private void printJobTable(Set<Job> jobs) {
        int columnWidth = 25; // Adjust this based on your needs
        String horizontalLine = "+-" + "-".repeat(13) + "-+-" + "-".repeat(8) + "-+-" + "-".repeat(13) + "-+-" + "-".repeat(60) + "-+";

        // Print top border
        System.out.println(horizontalLine);

        // Print table header
        System.out.printf("| %-13s | %-8s | %-13s | %-63s |%n", "Appeared", "Price", "Due Date", "Title");

        // Print header separator
        System.out.println(horizontalLine);


        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

        for (Job job : jobs) {
            String appearedFormatted = job.getAppeared().format(outputFormatter);
            String dueDateFormatted = job.getDueDate().format(outputFormatter);

            // Remove newline characters from the price
            String price = job.getPrice().replace("\n", "");

            // Shorten title if necessary
            String title = job.getTitle().length() > 60 ? job.getTitle().substring(0, 57) + "..." : job.getTitle();

            // Print job details with borders
            System.out.printf("| %-13s | %-8s | %-13s | %-43s |%n", appearedFormatted, price, dueDateFormatted, title);

            // Print separator
            System.out.println(horizontalLine);
        }

        // Print bottom border
        System.out.println(horizontalLine);
    }
}
