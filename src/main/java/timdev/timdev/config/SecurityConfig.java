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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import timdev.timdev.enums.RoleType;
import timdev.timdev.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter, 
        CustomUserDetailsService customUserDetailsService,
        CustomAuthSuccessHandler customAuthSuccessHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthSuccessHandler = customAuthSuccessHandler;
    }

    @SuppressWarnings({ "deprecation", "removal" })
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth

                // ========================
                // PUBLIC API (NO LOGIN)
                // ========================
                .requestMatchers("/api/v1/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/sv/uploads/**").permitAll()
                .requestMatchers("/api/v1/uploads/**").permitAll()

                // ========================
                // PUBLIC WEB
                // ========================
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

                // ========================
                // SECURED WEB (LOGIN REQUIRED)
                // ========================
                .requestMatchers("/admin/**").hasRole(RoleType.ADMIN.toString())

                .anyRequest().authenticated()
            )

            // Form Login (Web only)
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(customAuthSuccessHandler)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            .rememberMe(rememberMe -> rememberMe
                .key("uniqueAndSecretKey")   // use a strong secret key
                .tokenValiditySeconds(Integer.MAX_VALUE) 
                .rememberMeParameter("remember-me") // name of checkbox in login form
                .userDetailsService(customUserDetailsService)
            )

            // Disable CSRF for API
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/v1/**")
            )

            // ❗ REMOVE JWT FILTER if API is public
            // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            .authenticationProvider(authenticationProvider());

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