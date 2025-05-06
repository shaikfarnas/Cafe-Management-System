package com.inn.cafe.JWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    // Use NoOpPasswordEncoder for development (NOT recommended for production)
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    // Configure the AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.userDetailsService(customerUsersDetailsService); // Use custom user details service
        return auth.build();
    }

    // Security filter chain configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http.cors().configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.addAllowedOrigin("http://localhost:4200"); // Allow Angular frontend
                    configuration.addAllowedMethod("*"); // Allow all HTTP methods
                    configuration.addAllowedHeader("*"); // Allow all headers
                    configuration.setAllowCredentials(true); // Allow credentials
                    return configuration;
                })
                .and()
                .csrf().disable() // Disable CSRF for stateless JWT applications

                // Configure endpoint access rules
                .authorizeRequests()
                .requestMatchers("/user/login", "/user/signup", "/user/forgotPassword").permitAll() // Public endpoints
                .requestMatchers("/user/checkToken").authenticated() // Secure /user/checkToken for 'USER' role
                .requestMatchers("/product/update").hasRole("ADMIN") // Secure /product/update for 'ADMIN' role
                .anyRequest().authenticated() // All other requests require authentication
                .and()

                // Stateless session management
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
