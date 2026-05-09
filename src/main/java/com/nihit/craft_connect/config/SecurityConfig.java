package com.nihit.craft_connect.config;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfig extends WebMvcConfigurationSupport {

    @Autowired
    private CustomUserDetailService customUserDetailService;

    private final JwtAuthenticationFilter filter;
    @Autowired
    private JwtAuthenticationEntryPoint point;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver exceptionResolver;
    private static final String[] UN_SECURED_URLs = {
            "/api/v1/auth/**",
            "/api/v1/article/category/{title}",
            "/api/v1/article/articles",
            "/api/v1/category/{categoryId}",
            "/api/v1/category/categories",
            "/api/v1/article/{articleId}",
            "/api/v1/comment/article/{articleId}",
            "/api/v1/comment/comments",
            "/api/v1/comment/{commentId}",
            "/api/v1/ad/createAd",
            "/api/v1/ad/image/{adId}",
            "/api/v1/ad/{adId}",
            "/api/v1/ad/homeAds",
            "/api/v1/ad/applicationAds",
            "/api/v1/ad/articleAds",
            "/api/v1/ad/ads",
            "/api/v1/createLegalAndSupport",
            "/api/v1/legalAndSupports",
            "/api/v1/user/register"

    };
    private static final String[] REPORTER_SECURED_URLs = {
            "/api/v1/article/postArticle",
            "/api/v1/article/user/articles"

    };
    private static final String[] ADMIN_SECURED_URLs = {
            "/api/v1/category/create",
            "/api/v1/category/update/{categoryId}",
            "/api/v1/category/delete/{categoryId}",
            "/api/v1/ad/updateAd/{adId}",
            "/api/v1/update/{id}"

    };
    public SecurityConfig(JwtAuthenticationFilter filter) {
        this.filter = filter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
    @Bean
    public AuthenticationProvider userAuthenticationProvider() {
        var authenticationProvider = new DaoAuthenticationProvider(customUserDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(userAuthenticationProvider()));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
               .cors(Customizer.withDefaults())
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(UN_SECURED_URLs).permitAll()

                )
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(REPORTER_SECURED_URLs).hasAuthority("ROLE_REPORTER")

                )
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(ADMIN_SECURED_URLs).hasAuthority("ROLE_ADMIN")

                )
                .authorizeHttpRequests((auth)->auth
                        .requestMatchers("/api/v1/user/**","/api/v1/article/{articleId}/like","/api/v1/article/disable/{articleId}","/api/v1/article/disabledArticles","/api/v1/comment/**","/api/v1/like/**").authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                )

                .exceptionHandling(ex -> ex.authenticationEntryPoint(point))
                .authenticationProvider(userAuthenticationProvider()).addFilterBefore( filter, UsernamePasswordAuthenticationFilter.class);



        return http.build();

    }



}
