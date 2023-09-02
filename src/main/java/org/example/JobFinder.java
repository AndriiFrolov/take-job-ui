package org.example;

import org.example.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

//@EnableJpaRepositories
@SpringBootApplication(scanBasePackageClasses = {Config.class})
public class JobFinder {
    private static ConfigurableApplicationContext applicationContext;
    public static boolean isRunning =false;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(JobFinder.class, args);
    }
}
