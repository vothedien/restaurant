package com.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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

                // Internal
                .requestMatchers("/api/tables/**").hasAnyRole("WAITER", "ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("WAITER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/orders/*/checkout").hasAnyRole("CASHIER", "ADMIN")

                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails waiter = User.builder()
                .username("waiter")
                .password(encoder.encode("waiter123"))
                .roles("WAITER")
                .build();

        UserDetails cashier = User.builder()
                .username("cashier")
                .password(encoder.encode("cashier123"))
                .roles("CASHIER")
                .build();

        return new InMemoryUserDetailsManager(admin, waiter, cashier);
    }
}
