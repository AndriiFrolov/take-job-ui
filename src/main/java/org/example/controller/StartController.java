package org.example.controller;

import org.example.dto.ConfigurationDto;
import org.example.dto.Job;
import org.example.dto.Status;
import org.example.service.EmailService;
import org.example.service.ErrorService;
import org.example.service.JobFinderRunner;
import org.example.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


@RestController
@RequestMapping("/api")
public class StartController {

    @Autowired
    private JobFinderRunner jobService;

    @Autowired
    ErrorService errorService;

    @Autowired
    StatusService statusService;

    @PostMapping(path = "/start")
    @ResponseStatus(HttpStatus.CREATED)
    void start(@RequestBody ConfigurationDto configurationDto) {
        jobService.startScanning(configurationDto);
    }

    @PostMapping(path = "/stop")
    @ResponseStatus(HttpStatus.CREATED)
    void stop() {
        statusService.stop();
    }

    @GetMapping(path = "/status")
    Status getStatus() {
        Status status = statusService.getStatus();
        status.setErrors(errorService.getErrors());
        return status;
    }
}
