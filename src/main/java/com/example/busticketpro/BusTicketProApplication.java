package com.example.busticketpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BusTicketProApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusTicketProApplication.class, args);
    }

}
