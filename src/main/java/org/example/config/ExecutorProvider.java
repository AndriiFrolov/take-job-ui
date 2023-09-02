package org.example.config;

import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class ExecutorProvider {
    private ScheduledExecutorService executor;

    public ScheduledExecutorService getExecutor() {
        if (Objects.isNull(executor) || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        return executor;
    }
}
