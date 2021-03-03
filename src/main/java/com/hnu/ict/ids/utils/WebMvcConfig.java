package com.hnu.ict.ids.utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new CheckParamsInterceptor())
                .addPathPatterns("/**");
        //非空字段拦截
        super.addInterceptors(registry);
    }
}