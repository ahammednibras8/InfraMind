package com.inframind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class InframindApplication {

	public static void main(String[] args) {
		SpringApplication.run(InframindApplication.class, args);
	}

	// Single Live Endpoint: Version Info
	@GetMapping("/api/v1/status")
	@PreAuthorize("hasRole('ADMIN')")
	public String getStatus() {
		return "InfraMind Backend: Operational (v1.0.0)";
	}

	// Simple Public Endpoint for Health Check
	@GetMapping("/api/public/health")
	public String publicHealthCheck() {
		return "InfraMind Public Health: OK";
	}
}
