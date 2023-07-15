package com.ingreen.dbdiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestDbDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.from(DbDiscoveryApplication::main).with(TestDbDiscoveryApplication.class).run(args);
    }

}
