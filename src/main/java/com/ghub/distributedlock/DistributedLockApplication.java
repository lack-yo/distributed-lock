package com.ghub.distributedlock;

import com.ghub.distributedlock.provide.DistributeLockService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DistributedLockApplication {

    public static void main(String[] args) {
        //非 Web 应用
        new SpringApplicationBuilder(DistributeLockService.class)
                .web(false)
                .run(args);
    }

}
