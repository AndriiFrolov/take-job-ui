package org.example.service;


import jakarta.mail.MessagingException;
import org.example.dto.ConfigurationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ErrorService {

    @Autowired
    EmailService emailService;

    @Autowired
    StatusService statusService;
    private List<String> errors = new ArrayList<>();
    private ConfigurationDto configurationDto;

    public void clearErrors() {
        this.errors = new ArrayList<>();
    }

    public void addError(String error, boolean stop) {
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

    public List<String> getErrors() {
        return errors;
    }

    public void setConfiguration(ConfigurationDto configurationDto) {
        this.configurationDto = configurationDto;
    }
}
