package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class BackApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackApplication.class, args);
    }

    @Configuration
    public static class WebConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // 映射上传文件目录，使前端可以通过URL访问上传的文件
            // 使用绝对路径，确保在任何环境下都能正确访问
            String uploadPath = System.getProperty("user.dir") + "/uploads/";
            registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadPath);
            System.out.println("上传文件目录映射: " + uploadPath);
        }
    }

}