package com.inframind;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@SpringBootApplication
@RestController
@EnableMethodSecurity
public class InframindApplication {

    // Autowire RequestMappingHandlerMapping to access all registered endpoints
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    // Autowire MeterRegistry for custom metrics
    @Autowired
    private MeterRegistry meterRegistry;

    // Store application startup time for custom uptime metric
    private final Instant appStartupTime = Instant.now();

    public static void main(String[] args) {
        SpringApplication.run(InframindApplication.class, args);
    }

    // NEW: Register custom uptime metric as a Spring @Bean
    @Bean
    public Gauge inframindCustomUptimeSecondsGauge(MeterRegistry meterRegistry) {
        // Build and register the custom gauge
        Gauge gauge = Gauge.builder("inframind_custom_uptime_seconds", this, app -> {
            // Calculate uptime as current epoch second minus startup epoch second
            return (double) (Instant.now().getEpochSecond() - appStartupTime.getEpochSecond());
        })
        .description("Application uptime in seconds") // Human-readable description
        .tag("component", "core") // Add a tag for better categorization and filtering
        .register(meterRegistry); // Register with the MeterRegistry

        // Log confirmation that the metric has been registered via @Bean
        System.out.println("Custom uptime metrics 'inframind_custom_uptime_seconds' registered via @Bean");
        return gauge; // Return the registered gauge bean
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

    @GetMapping("/api/admin/report")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminReport() {
        return "Admin Report: Critical System Metrics (ADMIN ONLY)";
    }

    @PostMapping("/api/user/data")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String postUserData() {
        return "User Data Posted Successfully (USER or ADMIN)";
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
            List<String> rolesAllowed = new ArrayList<>();
            Method method = handlerMethod.getMethod();
            PreAuthorize preAuthorizeAnnotation = AnnotationUtils.findAnnotation(method, PreAuthorize.class);
            PreAuthorize classPreAuthorizeAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(),
                    PreAuthorize.class);

            if (preAuthorizeAnnotation != null) {
                rolesAllowed.addAll(extractRolesFromExpression(preAuthorizeAnnotation.value()));
            } else if (classPreAuthorizeAnnotation != null) {
                rolesAllowed.addAll(extractRolesFromExpression(classPreAuthorizeAnnotation.value()));
            } else {
                // If no @PreAuthorize, consider it public or unsecured by default for API map
                // purposes
                rolesAllowed.add("UNSECURED_OR_DEFAULT_SECURED");
            }
            if (rolesAllowed.isEmpty()) {
                rolesAllowed.add("UNSECURED_OR_DEFAULT_SECURED");
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

    // Objective: Show who has access to what, where, and why.
    @GetMapping("/infra/access-map")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccessMapEntry> getAccessMap() {
        List<AccessMapEntry> accessMap = new ArrayList<>();

        for (Map.Entry<RequestMappingInfo, org.springframework.web.method.HandlerMethod> entry : handlerMapping
                .getHandlerMethods().entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            org.springframework.web.method.HandlerMethod handlerMethod = entry.getValue();

            Set<String> patterns = mappingInfo.getPatternValues();
            Set<String> methods = mappingInfo.getMethodsCondition().getMethods().stream().map(Enum::name)
                    .collect(Collectors.toSet());

            // Extract roles required for this endpoint
            List<String> roles = new ArrayList<>();
            Method method = handlerMethod.getMethod();
            PreAuthorize preAuthorizeAnnotation = AnnotationUtils.findAnnotation(method, PreAuthorize.class);
            PreAuthorize classPreAuthorizeAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(),
                    PreAuthorize.class);

            if (preAuthorizeAnnotation != null) {
                roles.addAll(extractRolesFromExpression(preAuthorizeAnnotation.value()));
            } else if (classPreAuthorizeAnnotation != null) {
                roles.addAll(extractRolesFromExpression(classPreAuthorizeAnnotation.value()));
            } else {
                // For the access map, if no specific @PreAuthorize, we might default to no
                // roles specified
                roles.add("NONE_EXPLICITLY_REQUIRED");
            }
            if (roles.isEmpty()) {
                roles.add("NONE_EXPLICITLY_REQUIRED");
            }

            // For each pattern and method combination, create an entry
            for (String pattern : patterns) {
                for (String httpMethod : methods) {
                    accessMap.add(new AccessMapEntry(pattern, httpMethod, roles));
                }
            }
        }
        return accessMap;
    }

    // Helper Method to Extract Roles from @PreAuthorize Expression
    private List<String> extractRolesFromExpression(String expression) {
        List<String> roles = new ArrayList<>();

        Pattern pattern = Pattern.compile("'(ROLE_\\w+)'|'(\\w+)'|\\b(ROLE_\\w+)\\b|\\b(\\w+)\\b");
        Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                roles.add(matcher.group(1).replace("ROLE_", ""));
            } else if (matcher.group(2) != null) {
                roles.add(matcher.group(2));
            } else if (matcher.group(3) != null) {
                roles.add(matcher.group(3).replace("ROLE_", ""));
            } else if (matcher.group(4) != null) {
                roles.add(matcher.group(4));
            }
        }
        return roles.stream().distinct().collect(Collectors.toList());
    }

    public static class EndpointMetadata {
        public List<String> urls;
        public List<String> httpMethods;
        public String controllerMethods;
        public List<String> rolesAllowed;

        public EndpointMetadata(List<String> urls, List<String> httpMethods, String controllerMethod,
                List<String> rolesAllowed) {
            this.urls = urls;
            this.httpMethods = httpMethods;
            this.controllerMethods = controllerMethod;
            this.rolesAllowed = rolesAllowed;
        }
    }

    public static class AccessMapEntry {
        public String endpoint;
        public String method;
        public List<String> roles;

        public AccessMapEntry(String endpoint, String method, List<String> roles) {
            this.endpoint = endpoint;
            this.method = method;
            this.roles = roles;
        }
    }
}
