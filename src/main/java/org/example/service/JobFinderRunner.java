package org.example.service;


import jakarta.mail.MessagingException;
import org.example.config.ExecutorProvider;
import org.example.dto.ConfigurationDto;
import org.example.dto.Job;
import org.example.dto.Status;
import org.example.utils.DriverUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.example.config.Config.FORM_REFRESH_PERIOD_IN_SECONDS;
import static org.example.config.Config.TIMEOUT_FOR_INTERACTING_WITH_ELEMENT_IN_SECONDS;
import static org.example.utils.DriverUtils.*;

@Service
public class JobFinderRunner {

    public static final String NEW_JOBS_URL = "https://lcx-jobboard.lionbridge.com/new-jobs";
    public static int searchCount = 0;
    private final Logger logger = LoggerFactory.getLogger(JobFinderRunner.class);
    private final Timer timer = new Timer(true);
    @Autowired
    private ExecutorProvider executorProvider;

    @Autowired
    private EmailService emailService;

    private Set<Job> jobsFound = new HashSet<>();
    private static boolean isRunning = false;
    private static Integer refreshedPageTimes = 0;
    private static Integer takenJobs = 0;
    private static LocalDateTime startedAt;
    private static LocalDateTime stoppedAt;
    private static boolean shouldRun = false;

    public static void printJobTable(Set<Job> jobs) {
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

    public void startScanning(ConfigurationDto configurationDto) {

        logger.info("--------- Settings -------");
        logger.info("---minTotalPrice (EUR) " + configurationDto.getMinTotalPrice());
        logger.info("---deadlineHoursFromNow (days) " + configurationDto.getDeadlineHoursFromNow());
        logger.info("--------------------------");


        ScheduledExecutorService executor = executorProvider.getExecutor();

        executor.scheduleWithFixedDelay(() -> run(configurationDto), 0, FORM_REFRESH_PERIOD_IN_SECONDS, TimeUnit.SECONDS);
        logger.info("Scheduled the task at rate: {} ", FORM_REFRESH_PERIOD_IN_SECONDS);
    }

    protected void run(ConfigurationDto configurationDto) {
        shouldRun = true;
        isRunning = true;
        startedAt = LocalDateTime.now();
        takenJobs = 0;
        refreshedPageTimes = 0;
        while (shouldRun) {
            try {
                DriverUtils.getDriverInstance();
                boolean isFound = openNewJobs(configurationDto);
                if (isFound) {
                    takenJobs += 1;
                }
                if (configurationDto.getMaxAttempts().equals(takenJobs)) {
                    shouldRun = false;
                }
            } catch (Exception e) {
                logger.error("Exception occurred during the process, quitting.", e);
                if (isWebDriverRunning) {
                    try {
                        resetDriverGracefully();
                    } catch (Exception e2) {
                        logger.error("Exception occurred during the process, driver initializing", e2);
                    }
                }

            }
        }
        logger.info("End of process");
        isRunning = false;
        stoppedAt = LocalDateTime.now();
        DriverUtils.resetDriverGracefully();
        timer.cancel();
        executorProvider.getExecutor().shutdown();
    }

    protected void signIn(ConfigurationDto configurationDto) {
        WebDriver driver = getDriverInstance();
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("Starting to {}", methodName);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_FOR_INTERACTING_WITH_ELEMENT_IN_SECONDS));
        wait.until(webDriver -> {
            try {
                // Getting the home page
                logger.info(String.format("Entering username: %s", configurationDto.getUsername()));
                webDriver.findElement(By.id("Username")).sendKeys(configurationDto.getUsername());
                sleep(1);
                logger.info("Clicking Proceed button");
                webDriver.findElement(By.tagName("button")).click();
                sleep(1);
                logger.info(String.format("Entering password: %s", configurationDto.getPassword()));
                webDriver.findElement(By.id("passwordInput")).sendKeys(configurationDto.getPassword());
                logger.info("Clicking Submit button");
                webDriver.findElement(By.id("submitButton")).click();
                sleep(1);
                return true;
            } catch (Exception e) {
                logger.error("Exception occurred ", e);
                return false;
            }
        });
    }

    protected boolean openNewJobs(ConfigurationDto configurationDto) {
        refreshedPageTimes++;
        WebDriver driver = getDriverInstance();
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("Starting to {}", methodName);

        logger.info(String.format("Getting the URL: %s", NEW_JOBS_URL));
        driver.get(NEW_JOBS_URL);

        sleep(4);
        logger.info("Checking if Sign in is required");
        if (driver.findElements(By.id("Username")).size() > 0) {
            logger.info("Sign in is required");
            signIn(configurationDto);
        }
        logger.info("Checking if no jobs message shown");
        List<WebElement> noJobsMessage = driver.findElements(By.xpath("//div[contains(@class, 'no-jobs-message')]"));
        logger.info("No Jobs message - " + noJobsMessage.size());
        if (noJobsMessage.size() > 0) {
            logger.info("No jobs available");
            sleep(configurationDto.getSecondsBetweenRepeat());
        } else {
            logger.info("Some jobs are available?");

            List<WebElement> cards = driver.findElements(By.xpath("//ltx-job-card"));


            logger.info("----------------------------------------------------");
            logger.info("----------------------------------------------------");
            logger.info("----------------------------------------------------");
            logger.info("------------Found {} cards ----------", cards.size());
            for (int i = 1; i <= cards.size(); i++) {
                try {
                    logger.info("----- Parsing job card {}", i);
                    Job job = new Job();
                    String card = String.format("(//ltx-job-card)[%d]", i);
                    WebElement title = driver.findElement(By.xpath(card + "//div[@class='job-title']"));
                    job.setTitle(title.getText());
                    job.setAppeared(LocalDateTime.now());
                    job.setCustomer(driver.findElement(By.xpath(card + "//div[@class='customerGroupName']")).getText());
                    job.setPrice(driver.findElement(By.xpath(card + "//div[@class='job-total']//span[@class='cost'] ")).getText());
                    job.setWordsCount(driver.findElement(By.xpath(card + "//div[@class='job-total']//div[@class='details']")).getText());
                    job.setDueDateStr(driver.findElement(By.xpath(card + "//div[@class='job-date']")).getText());
                    jobsFound.add(job);
                    boolean jobMatches = new JobMatcher().isJobMatches(job, configurationDto);
                    if (jobMatches) {
                        logger.info("!!!!! JOB MATCHES! {}", job.getTitle());
                        title.click();
                        sleep(1);
                        driver.findElement(By.xpath("(//button[@id='accept-btn'])[1]")).click();
                        logger.info("ACCEPTED!");
                        emailService.sendEmail(configurationDto, job);
                        sleep(1);
                        return true;
                    } else {
                        logger.info("JOB NOT MATCHES {}", job.getTitle());
                    }

                } catch (Exception e) {
                    logger.info("Error while parsing job card. Perhaps someone accepted it", e);
                }

            }

            printJobTable(jobsFound);
        }
        return false;

    }

    public void stop() {
        shouldRun = false;
    }

    public Status getStatus() {
        Status status = new Status();
        status.setFoundJobs(jobsFound);
        status.setRunning(isRunning);
        status.setStartedAt(startedAt);
        status.setStoppedAt(stoppedAt);
        status.setRefreshedPageTimes(refreshedPageTimes);
        status.setTakenJobs(takenJobs);

        String seleniumDriverHost = System.getenv().getOrDefault("SELENIUM_GRID_HOST", "localhost");
        status.setLinkToVideo(String.format("http://%s:7900/?autoconnect=1&resize=scale&password=secret", seleniumDriverHost));
        return status;
    }
}
