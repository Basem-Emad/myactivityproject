package com.ActivityTracking.User_Management.User;

import jakarta.persistence.Id;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.Month;

@Configuration
public class UserConfig {
    @Bean
    CommandLineRunner commandLineRunner(
            UserRepository repository){
        return args -> {
          User_ Ahmed=   new User_(
                    "Ahmed",
                    "332123",
                    "Ahmed@gmail.com",
                    LocalDate.of(2001, Month.NOVEMBER,7),
                    Gender.Male
            );
          User_ Mohamed=new User_(
                  "Mohamed",
                  "543232",
                  "Mohamed@gmail.con",
                  LocalDate.of(1993,Month.AUGUST,7),
                  Gender.Male
          );
            repository.saveAll(
                    List.of(Ahmed,Mohamed)
            );

        };
    }

    }

