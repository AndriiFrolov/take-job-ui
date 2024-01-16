package org.example.service;


import org.apache.commons.io.FileUtils;
import org.example.dto.LogDto;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LogsService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    public List<LogDto> readLogsFromLocalFiles() {
        try {
            List<LogDto> result = new ArrayList<>();
            List<String> lines = FileUtils.readLines(new File("logs/spring-boot-logger.log"), "UTF-8");
            LogDto lastLogDto = new LogDto();
            for (String logLine : lines) {
                String[] s = logLine.split(" ");
                LogDto logDto = new LogDto();
                if (s.length > 4 && isLineContainsDateAndTime(logLine)) {
                    logDto.setDate(s[0]);
                    logDto.setTime(s[1]);
                    logDto.setLogLevel(s[2]);
                    logDto.setSource(s[3]);
                    logDto.setThread(s[4]);
                    StringJoiner message = new StringJoiner(" ");
                    for (int i = 5; i < s.length; i++) {
                        message.add(s[i]);
                    }
                    logDto.setMessage(message.toString());
                    result.add(logDto);
                    lastLogDto = logDto;
                } else {
                    lastLogDto.append(logLine);
                }
                logDto.setFullyFilled(
                        Objects.nonNull(logDto.getDate()) &&
                                Objects.nonNull(logDto.getTime()) &&
                                Objects.nonNull(logDto.getLogLevel()) &&
                                Objects.nonNull(logDto.getSource()) &&
                                Objects.nonNull(logDto.getThread()) &&
                                Objects.nonNull(logDto.getMessage())
                );

            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Could not read log file", e);
        }
    }

    public void deleteLogs() {
        String folderPath = "logs/archived"; // Replace with the actual path to your folder

        long folderSize = getFolderSize(new File(folderPath));
        System.out.println("Archive Folder size: " + folderSize + " bytes");

        long threshold = 100 * 1024 * 1024; // 100 MB in bytes

        if (folderSize > threshold) {
            deleteFilesInFolder(new File(folderPath));
            System.out.println("Files deleted because folder size exceeded 100 MB.");
        }
    }

    public String getFolderSizeStr(File folder) {
        long sizeInBytes = getFolderSize(folder);
        long sizeInMegabytes = sizeInBytes / (1024 * 1024);
        deleteLogs();

        return String.valueOf(sizeInMegabytes) + " MB";
    }

    public long getFolderSize(File folder) {
        long size = 0;
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += getFolderSize(file);
                }
            }
        }

        return size;
    }

    private void deleteFilesInFolder(File folder) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                    System.out.println("Deleted: " + file.getAbsolutePath());
                }
            }
        }
    }

    public List<LogDto> getLogs(String threadName, Date start, Date finish) {
        List<LogDto> logDtos = readLogsFromLocalFiles();
        List<LogDto> filteredLogDtos = logDtos.stream().filter(logDto -> {
            try {
                Date logDate = DATE_FORMAT.parse(logDto.getDate() + " " + logDto.getTime());
                return (logDto.getThread().contains(threadName))
                        && (logDate.equals(start) || logDate.after(start))
                        && (logDate.equals(finish) || logDate.before(finish));
            } catch (ParseException e) {
                throw new RuntimeException("Could not parse log message {}", e);
            }
        }).collect(Collectors.toList());
        return filteredLogDtos;
    }

    private boolean isLineContainsDateAndTime(String line) {
        String[] s = line.split(" ");
        if (s.length < 2) {
            return false;
        }
        try {
            DATE_FORMAT.parse(s[0] + " " + s[1]);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

}
