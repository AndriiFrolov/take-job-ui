package org.example.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.config.Config;
import org.example.service.ErrorService;
import org.example.service.StatusService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Objects;

@Service
public class DriverService {
    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);
    private WebDriver driver;
    private boolean isWebDriverRunning = false;

    @Autowired
    private ErrorService errorService;

    @Autowired
    private StatusService statusService;

    public static void sleep(long sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        if (!Config.IS_BROWSER_VISIBLE) {
            options.addArguments("--headless");
        }
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        // Add options to make Selenium-driven browser look more like a regular user's browser
        options.addArguments("--disable-blink-features=AutomationControlled"); // Remove "navigator.webdriver" flag
        options.addArguments("--disable-infobars"); // Disable infobars
        options.addArguments("--start-maximized"); // Start the browser maximized
        options.addArguments("--disable-extensions"); // Disable extensions

        // Add a fake user-agent to make it look like a regular browser
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

        return options;
    }

    public WebDriver makeSureDriverIsRunning() {
        if (Objects.isNull(driver)) {
            driver = initDriver();
        } else if (!isWebDriverRunning) {
            resetDriverGracefully();
        }
        return driver;
    }

    private WebDriver initDriver() {
        logger.info("Initialising new web driver session.");
        String seleniumDriverHost = System.getenv().getOrDefault("SELENIUM_GRID_HOST", "localhost");
        String remoteUrl = "http://" + seleniumDriverHost + ":4444/wd/hub";
        driver = null;

        //ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        for (int i = 0; i < 1 && !isWebDriverRunning; i++) {
            try {
                ChromeOptions chromeOptions = getChromeOptions();
                if (!Config.IS_BROWSER_VISIBLE) {
                    chromeOptions.addArguments("--headless");
                }
                if (Config.IS_LOCAL_CHROME) {
                    logger.info("Initializing local driver");
                    WebDriverManager.chromedriver().setup();
                    driver = new ChromeDriver(chromeOptions);
                } else {
                    logger.info("Initializing remote driver. Host: {}, Attempt: {}", seleniumDriverHost, i);
                    driver = new RemoteWebDriver(new URL(remoteUrl), chromeOptions);
                    driver.manage().window().maximize();
                    logger.info("Driver is initialized.");
                }
                isWebDriverRunning = true;
                break;
            } catch (Exception e) {
                logger.error("Failed to initialize the driver. Reason: ", e);
            }
        }
        if (!isWebDriverRunning) {
            errorService.addError("Could not initialize driver", true);
            driver = null;
        }

        return driver;
    }

    public void resetDriverGracefully() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("Starting to {}", methodName);
        try {
            if (Objects.isNull(driver)) {
                logger.info("Driver is null, so no quitting");
            } else if (!isWebDriverRunning) {
                logger.info("Driver is already stopped. Setting it to null");
                driver = null;
            } else {
                this.driver.close();
                this.driver.quit();
                logger.info("Successfully reset the driver");
            }

        } catch (org.openqa.selenium.NoSuchSessionException noSuchSessionException) {
            errorService.addError("Got NoSuchSessionException", false);
            logger.error("Got NoSuchSessionException: ", noSuchSessionException);
        } catch (Exception e) {
            logger.error("Failed to reset the driver. Reason: ", e);
        } finally {
            isWebDriverRunning = false;
        }
    }

}
