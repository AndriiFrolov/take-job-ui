package org.example.service;


import jakarta.mail.MessagingException;
import org.example.dto.ConfigurationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ErrorService {
    private final Logger logger = LoggerFactory.getLogger(ErrorService.class);

    @Autowired
    EmailService emailService;

    @Autowired
    StatusService statusService;
    private List<String> errors = new ArrayList<>();
    private ConfigurationDto configurationDto;
    private Integer webDriverFailures = 0;

    public void clearErrors() {
        this.errors = new ArrayList<>();
        webDriverFailures = 0;
    }

    public void resetWebDriverFailures() {
        webDriverFailures = 0;
    }

    public void addError(String error, boolean stop) {
        logger.error((error));
        this.errors.add(error);
        if (stop) {
            statusService.stop();
            this.errors.add("Application stopped due to errors above");
            try {
                emailService.sendErrorEmail(this.configurationDto, getErrors());
            } catch (MessagingException e) {
                errors.add("Could not send e-mail");
            }
        }

    }

    public void registerWebDriverFailure() {
        logger.error("Increasing # of web driver failures by 1");
        this.webDriverFailures++;
        if (this.webDriverFailures > 5) {
            addError("Web driver failed " + this.webDriverFailures + " in a row. Hard-stop of execution", true);

        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setConfiguration(ConfigurationDto configurationDto) {
        this.configurationDto = configurationDto;
    }
}
