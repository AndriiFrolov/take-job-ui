package org.example.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.config.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.example.config.Config.TIMEOUT_FOR_INTERACTING_WITH_ELEMENT_IN_SECONDS;

public class DriverUtils {
    private static final Logger logger = LoggerFactory.getLogger(DriverUtils.class);
    public static boolean isWebDriverRunning = false;
    private static WebDriver driver;

    private DriverUtils() {
    }

    public static WebDriver getDriverInstance() {
        if (Objects.nonNull(driver) && isWebDriverRunning) {
            return driver;
        } else {
            if (isWebDriverRunning) {
                resetDriverGracefully();
            }
            driver = initDriver();
        }
        return driver;
    }

    public static void sleep(long sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static WebDriver initDriver() {
        String seleniumDriverHost = System.getenv().getOrDefault("SELENIUM_GRID_HOST", "localhost");
        String remoteUrl = "http://" + seleniumDriverHost + ":4444/wd/hub";
        driver = null;
        int i = 0;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        while (i < TIMEOUT_FOR_INTERACTING_WITH_ELEMENT_IN_SECONDS) {
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
                    logger.info("Initializing remote driver. Host: {}, try: {}", seleniumDriverHost, i);
                    driver = new RemoteWebDriver(new URL(remoteUrl), chromeOptions);
                    driver.manage().window().maximize();
                    logger.info("Driver is initialized.");
                }
                break;
            } catch (Exception e) {
                logger.error("Failed to initialize the driver. Reason: ", e);
                try {
                    executor.schedule(() -> {
                    }, 1, TimeUnit.SECONDS).get();
                } catch (InterruptedException | ExecutionException ex) {
                    logger.error("Failed to wait for 1 second. Reason: ", ex);
                }
                i++;
            }
        }
        executor.shutdownNow();
        isWebDriverRunning = true;
        return driver;
    }


    public static void resetDriverGracefully() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("Starting to {}", methodName);
        try {
            getDriverInstance().close();
            getDriverInstance().quit();
            isWebDriverRunning = false;
            logger.info("Successfully reset the driver");

        } catch (Exception e) {
            logger.error("Failed to reset the driver. Reason: ", e);
        }
    }

    public static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        if (!Config.IS_BROWSER_VISIBLE) {
            options.addArguments("--headless");
        }
        options.addArguments("--disable-dev-shm-usage");

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

}
