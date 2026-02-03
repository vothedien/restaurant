package com.restaurant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
@Bean
CommandLineRunner printHashes(PasswordEncoder encoder) {
    return args -> {
        System.out.println("HASH(admin123)  = " + encoder.encode("admin123"));
        System.out.println("HASH(waiter123) = " + encoder.encode("waiter123"));
        System.out.println("HASH(cashier123)= " + encoder.encode("cashier123"));
    };
}

	
	
}

