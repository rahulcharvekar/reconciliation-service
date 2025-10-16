package com.example.paymentreconciliation;

import com.shared.security.EnableSharedSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.paymentreconciliation", "com.shared"})
@EnableSharedSecurity
public class ReconciliationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReconciliationServiceApplication.class, args);
    }
}
