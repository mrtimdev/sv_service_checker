package timdev.timdev.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import timdev.timdev.enums.RoleType;
import timdev.timdev.listener.SessionTracker;
import timdev.timdev.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final CustomSessionInformationExpiredStrategy customSessionInformationExpiredStrategy;
    private final SessionTracker sessionTracker;
    
    @Lazy
    @Autowired
    private SessionValidationFilter sessionValidationFilter;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter, 
        CustomUserDetailsService customUserDetailsService,
        CustomAuthenticationFailureHandler customAuthFailureHandler,
        CustomSessionInformationExpiredStrategy customSessionInformationExpiredStrategy,
        SessionTracker sessionTracker
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationFailureHandler = customAuthFailureHandler;
        this.customSessionInformationExpiredStrategy = customSessionInformationExpiredStrategy;
        this.sessionTracker = sessionTracker;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/auth/login",
                    "/auth/logout",
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
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                
                .requestMatchers("/ws/**").permitAll()
                
                // Admin endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/auth/logout","/api/v1/**", "/ws/**")
            )
            
            // Form login
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureHandler(customAuthenticationFailureHandler)
                .successHandler((request, response, authentication) -> {
                    HttpSession session = request.getSession();
                    String sessionId = session.getId();
                    String username = authentication.getName();
                    sessionTracker.trackLogin(username, sessionId);
                    response.sendRedirect("/admin/dashboard");
                })
                .permitAll()
            )
            
            // Logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout", "POST"))
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            
            // Session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredSessionStrategy(customSessionInformationExpiredStrategy)
                .sessionRegistry(sessionRegistry())
            )
            
            // Remember me
            .rememberMe(rememberMe -> rememberMe
                .key("uniqueAndSecretKey")
                .tokenValiditySeconds(Integer.MAX_VALUE) 
                .rememberMeParameter("remember-me")
                .userDetailsService(customUserDetailsService)
            )
            
            // Add filters
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            .addFilterAfter((request, response, chain) -> {
                HttpSession session = ((HttpServletRequest) request).getSession(false);
                if (session != null && sessionTracker != null) {
                    sessionTracker.updateActivity(session.getId());
                }
                chain.doFilter(request, response);
            }, UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

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