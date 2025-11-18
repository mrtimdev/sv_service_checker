package timdev.timdev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import timdev.timdev.enums.RoleType;
import timdev.timdev.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;

    }

    @SuppressWarnings({ "removal", "deprecation" })
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Split configuration for API and Web
            .securityMatcher("/api/v1/**", "/**")
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/",
                    "/auth/login",
                    "/auth/register",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/error"
                ).permitAll()
                
                // API endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .requestMatchers("/api/v1/**")
                .hasAnyRole(
                    RoleType.ADMIN.toString(),
                    RoleType.MANAGER.toString(),
                    RoleType.USER.toString()
                    
                )

                // Web endpoints
                .requestMatchers("/**")
                .hasAnyRole(
                    RoleType.ADMIN.toString(),
                    RoleType.MANAGER.toString(),
                    RoleType.USER.toString()
                    
                )

                .anyRequest().authenticated()
            )
            
            // Form login configuration for Thymeleaf
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            
            // Logout configuration
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me") 
                .permitAll()
            )
            
            // Session management
            // .sessionManagement(session -> session
            //     .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // For Thymeleaf
            //     .maximumSessions(1)
            //     .maxSessionsPreventsLogin(false)
            // )

            .rememberMe(rememberMe -> rememberMe
                .key("uniqueAndSecretKey")   // use a strong secret key
                .tokenValiditySeconds(Integer.MAX_VALUE) 
                .rememberMeParameter("remember-me") // name of checkbox in login form
                .userDetailsService(customUserDetailsService)
            )
            
            // API specific configurations
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/v1/**") // Disable CSRF for API
            )
            
            // Add JWT filter for API requests
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @SuppressWarnings("deprecation")
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}