package com.javarush.jira;

import com.javarush.jira.common.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:secrets.properties")
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableCaching
public class JiraRushApplication {

    public static void main(String[] args) {
        try {
            System.out.println("Starting Spring Boot application...");
            SpringApplication.run(JiraRushApplication.class, args);// после отладки удалить все кроме этой строчки
            System.out.println("Application started successfully.");
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
