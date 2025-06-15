package com.inframind;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
@RestController
public class InframindApplication {

	@Autowired
	@Qualifier("requestMappingHandlerMapping")
	private RequestMappingHandlerMapping handlerMapping;

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

	// Return a JSON list of all available endpoints and their metadata
	@GetMapping("/infra/api-map")
	@PreAuthorize("hasRole('ADMIN')")
	public List<EndpointMetadata> getApiMap() {
		List<EndpointMetadata> apiMap = new ArrayList<>();

		// Get all registered request mappings
		for (Map.Entry<RequestMappingInfo, org.springframework.web.method.HandlerMethod> entry : handlerMapping
				.getHandlerMethods().entrySet()) {
			RequestMappingInfo mappingInfo = entry.getKey();
			org.springframework.web.method.HandlerMethod handlerMethod = entry.getValue();

			// Extract HTTP method(s)
			Set<String> methods = mappingInfo.getMethodsCondition().getMethods().stream().map(Enum::name)
					.collect(Collectors.toSet());

			// Extract URL patterns
			Set<String> patterns = mappingInfo.getPatternValues();

			// Extract controller method name
			String controllerMethod = handlerMethod.getMethod().getName();
			String controllerClass = handlerMethod.getBeanType().getName();

			// Determine Roles Allowed
			String rolesAllowed = "PUBLIC";
			Method method = handlerMethod.getMethod();
			PreAuthorize preAuthorizeAnnotation = AnnotationUtils.findAnnotation(method, PreAuthorize.class);

			if (preAuthorizeAnnotation != null) {
				rolesAllowed = preAuthorizeAnnotation.value();
			} else {
				PreAuthorize classPreAuthorizeAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(),
						PreAuthorize.class);
				if (classPreAuthorizeAnnotation != null) {
					rolesAllowed = classPreAuthorizeAnnotation.value();
				}
			}

			EndpointMetadata metadata = new EndpointMetadata(
					patterns.isEmpty() ? List.of("") : new ArrayList<>(patterns),
					methods.isEmpty() ? List.of("ANY") : new ArrayList<>(methods),
					controllerClass + "::" + controllerMethod,
					rolesAllowed);
			apiMap.add(metadata);
		}

		return apiMap;
	}

	public static class EndpointMetadata {
		public List<String> urls;
		public List<String> httpMethods;
		public String controllerMethods;
		public String rolesAllowed;

		public EndpointMetadata(List<String> urls, List<String> httpMethods, String controllerMethod,
				String rolesAllowed) {
			this.urls = urls;
			this.httpMethods = httpMethods;
			this.controllerMethods = controllerMethod;
			this.rolesAllowed = rolesAllowed;
		}
	}
}
