package com.signflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
        }
)
@EnableFeignClients(basePackages = {"com.signflow.adapter", "com.signflow.infrastructure"})
@EnableScheduling
public class SignFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignFlowApplication.class, args);
    }

}
