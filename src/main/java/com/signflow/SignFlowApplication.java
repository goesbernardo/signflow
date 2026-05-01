package com.signflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
        }
)
@EnableFeignClients(basePackages = "com.signflow.adapter")
public class SignFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignFlowApplication.class, args);
    }

}
