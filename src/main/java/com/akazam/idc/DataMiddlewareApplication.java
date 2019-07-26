package com.akazam.idc;

import com.akazam.idc.constants.ConfigureProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@ServletComponentScan
public class DataMiddlewareApplication {

    @Autowired
    ConfigureProperties properties;
    
    public static void main(String[] args) {
        SpringApplication.run(DataMiddlewareApplication.class, args);
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(properties.getPoolSize());
        return threadPoolTaskScheduler;
    }


}

