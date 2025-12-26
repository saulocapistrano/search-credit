package br.com.searchcredit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "br.com.searchcredit.infrastructure.repository.jpa")
public class SearchCreditApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchCreditApplication.class, args);
    }

}
