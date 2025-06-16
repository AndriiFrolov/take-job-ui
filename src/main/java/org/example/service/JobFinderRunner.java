package org.example.service;


import org.example.config.ExecutorProvider;
import org.example.dto.ConfigurationDto;
import org.example.dto.Job;
import org.example.utils.DriverService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.example.config.Config.CURRENT_YEAR;
import static org.example.config.Config.FORM_REFRESH_PERIOD_IN_SECONDS;
import static org.example.utils.DriverService.sleep;

@Service
public class JobFinderRunner {

    public static final String NEW_JOBS_URL = "https://lcx-jobboard.lionbridge.com/new-jobs";

    private final Logger logger = LoggerFactory.getLogger(JobFinderRunner.class);

    @Autowired
    private ExecutorProvider executorProvider;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StatusService statusService;
    @Autowired
    private DriverService driverService;

    @Autowired
    private ErrorService errorService;

    public void startScanning(ConfigurationDto configurationDto) {

        logger.info("--------- Settings -------");
        logger.info("---minTotalPrice (EUR) " + configurationDto.getMinTotalPrice());
        logger.info("---deadlineHoursFromNow (days) " + configurationDto.getDeadlineHoursFromNow());
        logger.info("---emailToSend (string) " + configurationDto.getEmailToSend());
        logger.info("---maxAttempts (times) " + configurationDto.getMaxAttempts());
        logger.info("---secondsBetweenRepeat " + configurationDto.getSecondsBetweenRepeat());
        logger.info("--------------------------");


        ScheduledExecutorService executor = executorProvider.getExecutor();

        executor.scheduleWithFixedDelay(() -> run(configurationDto), 0, FORM_REFRESH_PERIOD_IN_SECONDS, TimeUnit.SECONDS);
        logger.info("Scheduled the task at rate: {} ", FORM_REFRESH_PERIOD_IN_SECONDS);
    }

    protected void run(ConfigurationDto configurationDto) {
        statusService.start();
        errorService.clearErrors();
        errorService.setConfiguration(configurationDto);

        while (statusService.isShouldRun()) {
            try {
                driverService.makeSureDriverIsRunning();
                boolean isFound = openNewJobs(configurationDto);
                if (isFound) {
                    statusService.tookAJob();
                }
                if (configurationDto.getMaxAttempts().equals(statusService.getTakenJobs())) {
                    statusService.markShouldNotRunAnymore();
                }
                errorService.resetWebDriverFailures(); //cause we succesfully reached this point
            } catch (Exception e) {
                logger.error("Exception occurred during the process, quitting.", e);
                errorService.registerWebDriverFailure();
                if (statusService.isRunning()) {
                    try {
                        driverService.resetDriverGracefully();
                    } catch (Exception e2) {
                        logger.error("Exception occurred during the process, driver initializing", e2);
                        errorService.registerWebDriverFailure();
                    }
                }
            }
        }
        logger.info("End of process");
        statusService.stop();
        driverService.resetDriverGracefully();

        executorProvider.getExecutor().shutdown();
    }

    protected boolean signIn(ConfigurationDto configurationDto) {
        WebDriver webDriver = driverService.makeSureDriverIsRunning();
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("Starting to {}", methodName);
        //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_FOR_INTERACTING_WITH_ELEMENT_IN_SECONDS));
        // wait.until(webDriver -> {
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
            if (webDriver.findElements(By.xpath("//span[@id='errorText']")).size() > 0) {
                errorService.addError("Password seems to be incorrect", true);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Exception occurred ", e);
            return false;
        }
    }//);

    protected boolean openNewJobs(ConfigurationDto configurationDto) {

        WebDriver driver = driverService.makeSureDriverIsRunning();
        if (driver == null) {
            logger.info("Issue with driver, so quitting");
            return false;
        }
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("Starting to {}", methodName);

        String currentUrl = driver.getCurrentUrl();
        long minutesPassedSinceLastPageRefresh = Duration.between(statusService.getLastTimeWhenPageRedreshed(), Instant.now()).toMinutes();
        if (NEW_JOBS_URL.equals(currentUrl) && minutesPassedSinceLastPageRefresh < 30) {
            logger.info("Not opening " + NEW_JOBS_URL + " because it is already opened. Looking for 'Refresh' button");
            logger.info("Time passed since last page refresh is " + minutesPassedSinceLastPageRefresh);
            List<WebElement> refreshButton = driver.findElements(By.xpath("//button[@class='refresh-jobs-btn' and not(@hidden)]"));
            if (refreshButton.size() > 0) {
                logger.info("Refresh button shown. Clicking it");
                refreshButton.get(0).click();
            } else {
                logger.info("Refresh button not shown. Waiting " + configurationDto.getSecondsBetweenRepeat() + " seconds before next try");
                sleep(configurationDto.getSecondsBetweenRepeat());
                return false;
            }
        } else{
            logger.info(String.format("Getting the URL: %s", NEW_JOBS_URL));
            driver.get(NEW_JOBS_URL);
            statusService.refreshedPage();
        }

        sleep(4);
        logger.info("Checking if Sign in is required");
        if (driver.findElements(By.id("Username")).size() > 0) {
            logger.info("Sign in is required");
            boolean isSuccessSignIn = signIn(configurationDto);
            if (!isSuccessSignIn) {
                logger.info("Attempting to sign in again");
                isSuccessSignIn = signIn(configurationDto);
            }
            if (!isSuccessSignIn) {
                logger.info("Failed to login 2nd time in a row");
                errorService.addError("Failed to login", true);
                return false;
            }
        }
        logger.info("Checking if no jobs message shown");
        List<WebElement> noJobsMessage = driver.findElements(By.xpath("//div[contains(@class, 'no-jobs-message')]"));
        logger.info("No Jobs message - " + noJobsMessage.size());
        if (noJobsMessage.size() > 0) {
            logger.info("No jobs available");
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
                    job.setPrice(driver.findElement(By.xpath(card + "//div[@class='job-total']//span[@class='cost']")).getText());
                    job.setWordsCount(driver.findElement(By.xpath(card + "//div[@class='job-total']//div[@class='details']")).getText());
                    Optional<LocalDateTime> parseDate = parseDate(driver.findElement(By.xpath(card + "//div[@class='job-date']")).getText());
                    if (parseDate.isEmpty()) {
                        return false;
                    }
                    job.setDueDate(parseDate.get());
                    statusService.addFoundJob(job);
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
        }
        logger.info("Waiting for " + configurationDto.getSecondsBetweenRepeat() + " seconds");
        sleep(configurationDto.getSecondsBetweenRepeat());
        return false;

    }

    private Optional<LocalDateTime> parseDate(String dueDateStr) {

        String inputWithYear = CURRENT_YEAR + dueDateStr;

        String format1 = "yyyy EEEE, MMMM d, h:mm a zzz";
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern(format1)
                .toFormatter();
        try {
            logger.info("Attempting to parse date  " + dueDateStr);
            TemporalAccessor temporalAccessor = formatter.parse(inputWithYear);
            return Optional.of(LocalDateTime.from(temporalAccessor));
        } catch (Exception e) {
            logger.error("Failed to parse date ", e);
            logger.info("Attempting to parse date again " + dueDateStr);
            try {
                return Optional.of(LocalDateTime.parse(inputWithYear, formatter));
            } catch (Exception e2) {
                logger.error("Failed to parse date again", e2);
            }
        }
        errorService.addError("Failed to parse date " + dueDateStr, true);
        return Optional.empty();
    }
}
