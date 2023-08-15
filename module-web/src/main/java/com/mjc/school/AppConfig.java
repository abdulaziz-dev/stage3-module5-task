package com.mjc.school;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableJpaAuditing
@EnableWebMvc
@EnableSwagger2
@ComponentScan(basePackages = {"com.mjc.school.repository",
        "com.mjc.school.service",
        "com.mjc.school.controller",
        "com.mjc.school"})
public class AppConfig extends SpringBootServletInitializer{

        @Override
        protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
            return builder.sources(AppConfig.class);
        }

        public static void main(String[] args) {
            SpringApplication.run(AppConfig.class, args);
        }

}
