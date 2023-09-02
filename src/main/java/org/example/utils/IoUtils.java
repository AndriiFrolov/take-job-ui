package org.example.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class IoUtils {

    private final static Logger logger = LoggerFactory.getLogger(IoUtils.class);

    public static boolean isLocalSaveEnabled = true;

    private IoUtils() {
    }

    public static void savePage(WebDriver driver, String pageDescriber, String suffix) {
        try {
            if (!isLocalSaveEnabled) {
                logger.info("Saving is disabled");
                return;
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String dateAsStr = dtf.format(now);
            String fileName = pageDescriber + "_" + dateAsStr + "_" + suffix;
            String pagesourceFileName = fileName + ".html";
            String screenshotFileName = fileName + ".png";
            logger.info("File name :{}, {}", pagesourceFileName, screenshotFileName);

            String content;
            try {
                logger.info("Getting the page content");
                content = driver.getPageSource();

            } catch (Exception exception) {
                logger.error("Error occurred during getting the page source. Reason: ", exception);
                return;
            }


            File sourceFile;
            try {
                sourceFile = saveSourceCodeToFile(content, pagesourceFileName);
            } catch (IOException e) {
                logger.error("Error occurred during IO operation. Exception: ", e);
                return;
            }
            File screenShotFile;
            try {
                screenShotFile = saveScreenshot(driver, screenshotFileName);
            } catch (IOException e) {
                logger.error("Error occurred during IO operation. Exception: ", e);
                return;
            }

        } catch (Exception e) {
            logger.info("Saving has failed. Reason: ", e);
        }
    }

    private static File saveSourceCodeToFile(String content, String fileName) throws IOException {
        logger.info("Saving source code to file");
        File file = new File(fileName);
        FileWriter fw;
        fw = new FileWriter(file);
        fw.write(content);
        fw.close();
        return file;
    }

    private static File saveScreenshot(WebDriver driver, String fileName) throws IOException {
        logger.info("Saving screenshot");
        File scrFile1 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File file = new File(fileName);
        FileUtils.copyFile(scrFile1, file);
        return file;
    }

}
