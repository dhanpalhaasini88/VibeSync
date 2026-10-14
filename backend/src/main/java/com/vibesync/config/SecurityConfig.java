package com.vibesync.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.DispatcherType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Arrays;

/**
 * SecurityConfig — defines which routes are public and which require login.
 *
 * Public routes (no token needed):
 *   - POST /api/auth/register  → create new account
 *   - POST /api/auth/login     → get JWT token
 *   - GET  /api/songs/**       → browse songs (so non-logged-in visitors can see music)
 *   - GET  /api/weather/**     → weather data
 *
 * Protected routes (need "Authorization: Bearer <token>"):
 *   - /api/favorites/**        → your saved favorites
 *   - /api/playlists/**        → your playlists
 *   - /api/events/**           → listening events tracking
 *   - /api/recommendations/**  → personalized recommendations
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for REST APIs, CSRF is for form-based logins)
            .csrf(csrf -> csrf.disable())

            // Enable CORS using our configuration below
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Define which routes require authentication
     .authorizeHttpRequests(auth -> auth
    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/songs", "/api/songs/**").permitAll()
    .requestMatchers("/api/weather/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/songs", "/api/songs/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.PUT, "/api/songs", "/api/songs/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.DELETE, "/api/songs", "/api/songs/**").hasRole("ADMIN")
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/").permitAll()
    .anyRequest().authenticated()
)

            // Use stateless sessions — no server-side sessions, token carries identity
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Insert our JWT filter before Spring's default username/password filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt password encoder — hashes passwords before storing in DB
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS configuration — allows React (port 5173) to call our Spring Boot API (port 8081)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
