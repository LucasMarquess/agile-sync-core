package com.agilesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class AgileSyncCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgileSyncCoreApplication.class, args);
	}

}
