package com.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())  
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
           .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public
                .requestMatchers("/api/health", "/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/menu/**").permitAll()

                // Auth check (login thật)
                .requestMatchers(HttpMethod.GET, "/api/me").hasAnyRole("ADMIN","WAITER","CASHIER")

                // ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET,  "/api/orders/*/bill").hasAnyRole("CASHIER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/orders/*/checkout").hasAnyRole("CASHIER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/orders/*/items/*/status").hasAnyRole("CASHIER","WAITER","ADMIN")

                .requestMatchers("/api/tables/**").hasAnyRole("CASHIER" ,"WAITER","ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("CASHIER","WAITER","ADMIN")

                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
}
