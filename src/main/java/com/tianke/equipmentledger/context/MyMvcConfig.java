package com.tianke.equipmentledger.context;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyMvcConfig implements  WebMvcConfigurer{

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")     //允许的路径
                .allowedMethods("*")     //允许的方法
                .allowedOrigins("http://192.168.102.73:5555")       //允许的网站
                .allowedHeaders("*")     //允许的请求头
                .allowCredentials(true);
    }



}
