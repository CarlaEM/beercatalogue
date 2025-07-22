package com.haufe.beercatalogue.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.haufe.beercatalogue.exception.CustomAccessDeniedHandler;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",       // Swagger UI static content
                    "/v3/api-docs/**",      // OpenAPI docs endpoint
                    "/swagger-ui.html"      // legacy URL, just in case
                ).permitAll()

                // Anonymous users: allow GET requests to beers and manufacturers
                .requestMatchers(HttpMethod.GET, "/api/beers/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/manufacturers/**").permitAll()

                // Manufacturer users: can edit their own data
                .requestMatchers(HttpMethod.PUT, "/api/manufacturers/**").hasAnyRole("ADMIN", "MANUFACTURER")
                .requestMatchers(HttpMethod.DELETE, "/api/manufacturers/**").hasAnyRole("ADMIN", "MANUFACTURER")
                .requestMatchers(HttpMethod.PUT, "/api/beers/**").hasAnyRole("ADMIN", "MANUFACTURER")
                .requestMatchers(HttpMethod.POST, "/api/beers/**").hasAnyRole("ADMIN", "MANUFACTURER")
                .requestMatchers(HttpMethod.DELETE, "/api/beers/**").hasAnyRole("ADMIN", "MANUFACTURER")

                // Admins can do anything
                .requestMatchers("/api/**").hasRole("ADMIN")

                // Any other requests must be authenticated
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .permitAll()
            )
            .httpBasic(basic -> {})
            .exceptionHandling(exception -> exception.accessDeniedHandler(accessDeniedHandler)
        );

        return http.build();
    }

}