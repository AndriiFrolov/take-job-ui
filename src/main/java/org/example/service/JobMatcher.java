package org.example.service;

import org.example.dto.ConfigurationDto;
import org.example.dto.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobMatcher {
    private final Logger logger = LoggerFactory.getLogger(JobMatcher.class);

    public boolean isJobMatches(Job job, ConfigurationDto configurationDto) throws Exception {
        logger.info("---------Checking if  job matches requirements " + job.getTitle() + " ----");
        boolean isDateMatches = isDateMatches(job.getDueDate(), configurationDto.getDeadlineHoursFromNow());
        logger.info("------------{} Date {}", isDateMatches ? "PASSED" : "NOT MATCHES", job.getDueDate());

        boolean isPriceMatches = isPriceMatches(job.getPrice(), configurationDto.getMinTotalPrice());
        logger.info("------------{} Price {}", isPriceMatches ? "PASSED" : "NOT MATCHES", job.getPrice());

        //boolean isWordCountMatches = isWordCountMatches(job.getWordsCount(), settings.getMaxVolume());
        //logger.info("---{} Word count {}", isWordCountMatches ? "PASSED" : "NOT MATCHES", job.getWordsCount());
        return isDateMatches & isPriceMatches ;//& isWordCountMatches;
    }

    private boolean isWordCountMatches(String wordsCount, Integer maxVolume) {
        if (wordsCount.contains("words")) {
            try {
                double floatValue = Double.parseDouble(wordsCount.replaceAll(" words", ""));
                System.out.println("Parsed floating-point value: " + floatValue);
                return floatValue <= maxVolume;
            } catch (NumberFormatException e) {
                System.out.println("Error parsing floating-point value: " + e.getMessage());
            }
        }
        return false;
    }

    private boolean isPriceMatches(String price, Integer minPrice) throws Exception {
        return minPrice <= parsePrice(price);
    }


    private boolean isDateMatches(LocalDateTime dueDate, Integer deadlineHoursFromNow) {
        try {
            LocalDateTime tomorrow = LocalDateTime.now().plusHours(deadlineHoursFromNow);

            return dueDate.isAfter(tomorrow) || dueDate.equals(tomorrow);
        } catch (Exception e) {
            System.out.println("Error parsing or calculating date: " + e.getMessage());
            return false;
        }
    }

    public Integer parsePrice(String price) throws Exception {
        // Define a regular expression pattern to match integers
        price = price.replaceAll(",", "");
        Pattern pattern = Pattern.compile("\\d+");

        // Create a Matcher object to find matches in the input string
        Matcher matcher = pattern.matcher(price);

        if (matcher.find()) {
            // Extract the matched integer as a string
            String integerString = matcher.group();

            // Convert the string to an actual integer
            int integerValue = Integer.parseInt(integerString);

            System.out.println("Extracted integer: " + integerValue);
            return integerValue;
        } else {
            throw new Exception("No integer price found in the input." + price);
        }
    }
}
