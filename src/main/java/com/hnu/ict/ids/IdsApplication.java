package com.hnu.ict.ids;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @description: 启动类
 * @author: yuanhx
 * @create: 2021/01/07
 */

@SpringBootApplication
@EnableSwagger2Doc
public class IdsApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdsApplication.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(IdsApplication.class);

    }
}
