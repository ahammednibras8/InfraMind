package com.inframind;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/api/public/health").permitAll()
                                                .requestMatchers("/actuator/health", "/actuator/info",
                                                                "/actuator/metrics", "/actuator/env",
                                                                "/actuator/prometheus")
                                                .permitAll()
                                                .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                                                "/v3/api-docs/**", "/api-docs/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .httpBasic(httpBasic -> {
                                });
                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(Arrays.asList("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
                // Define ADMIN user with ADMIN role
                UserDetails admin = User.builder()
                                .username("admin")
                                .password(passwordEncoder.encode("password"))
                                .roles("ADMIN")
                                .build();

                UserDetails user = User.builder()
                                .username("user")
                                .password(passwordEncoder.encode("userpass"))
                                .roles("USER")
                                .build();

                return new InMemoryUserDetailsManager(admin, user);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
