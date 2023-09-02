package org.example.controller;


import org.example.dto.LogDto;
import org.example.service.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogsController {

    @Autowired
    private LogsService logsService;

    @GetMapping
    public List<LogDto> getLogs() {
        return logsService.readLogsFromLocalFiles();
    }
}
