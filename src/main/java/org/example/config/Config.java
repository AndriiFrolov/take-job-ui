package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Year;

@Configuration
@ComponentScan("org.example")
public class Config {

    public static long FORM_REFRESH_PERIOD_IN_SECONDS = 10;
    public static long TIMEOUT_FOR_INTERACTING_WITH_ELEMENT_IN_SECONDS = 120;
    public static String CURRENT_YEAR = (Year.now().getValue()+ " ");
    public static Boolean IS_LOCAL_CHROME = false;
    public static Boolean IS_BROWSER_VISIBLE = true;

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskScheduler() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("Executor-");
        executor.initialize();
        return executor;
    }
}

