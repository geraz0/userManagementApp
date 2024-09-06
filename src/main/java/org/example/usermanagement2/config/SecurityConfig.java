package org.example.usermanagement2.config;

import org.example.usermanagement2.model.User;
import org.example.usermanagement2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/users/register").permitAll() // Open to all
                        .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated() // User can update their details
                        .requestMatchers(HttpMethod.PUT, "/api/users/updateUser/{id}").hasRole("ADMIN") // Admin can update any user
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN") // Admin can view all users
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("ADMIN") // Ensure "hasRole('ADMIN')" matches with the role prefix
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/users/register",
                        "/api/users/me",
                        "/api/users/updateUser/{id}",
                        "/api/users/{id}"))  // Disable CSRF for specific endpoints
                .formLogin(form -> form
                        .loginPage("/login").permitAll()  // Custom login page
                )
                .httpBasic(withDefaults());  // Basic Auth for simplicity
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            // No need to add "ROLE_" prefix here, Spring Security does it automatically
            return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                    .password(user.getPassword())  // Password already encoded
                    .roles(user.getRole())  // Ensure role is "ADMIN" or "USER", no need to prefix "ROLE_"
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
