package com.fyp.autologistics.config;

import com.fyp.autologistics.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/blockchain-records/shipment/*").authenticated()
                        .requestMatchers("/api/blockchain-records/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/shipments").hasRole("MANUFACTURER")
                        .requestMatchers(HttpMethod.PUT, "/api/shipments/*/status").hasAnyRole("ADMIN", "MANUFACTURER", "LOGISTICS_PROVIDER")
                        .requestMatchers(HttpMethod.PUT, "/api/shipments/*/receipt-confirmation").hasRole("DEALERSHIP")
                        .requestMatchers(HttpMethod.PUT, "/api/shipments/*").hasAnyRole("ADMIN", "MANUFACTURER")
                        .requestMatchers(HttpMethod.DELETE, "/api/shipments/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/shipments/*/tracking-events").hasAnyRole("ADMIN", "LOGISTICS_PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/api/shipments/*/simulation/**").authenticated()
                        .requestMatchers("/api/simulation-sessions/**").hasAnyRole("ADMIN", "LOGISTICS_PROVIDER")

                        .requestMatchers(HttpMethod.GET, "/api/stock-monitoring/**").hasAnyRole("ADMIN", "MANUFACTURER", "DEALERSHIP")
                        .requestMatchers("/api/stock-monitoring/rules/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/alerts/*/acknowledge").hasAnyRole("ADMIN", "MANUFACTURER", "LOGISTICS_PROVIDER", "DEALERSHIP")
                        .requestMatchers(HttpMethod.PUT, "/api/alerts/*/resolve").hasAnyRole("ADMIN", "LOGISTICS_PROVIDER")

                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(customUserDetailsService);

        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
