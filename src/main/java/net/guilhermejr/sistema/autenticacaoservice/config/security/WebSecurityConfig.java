package net.guilhermejr.sistema.autenticacaoservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
//@EnableMethodSecurity
@EnableWebSecurity
public class WebSecurityConfig {

    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AuthenticationJwtFilter authenticationJwtFilter;

    public WebSecurityConfig(AuthenticationEntryPointImpl authenticationEntryPoint, AuthenticationJwtFilter authenticationJwtFilter) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.authenticationJwtFilter = authenticationJwtFilter;
    }

    private static final String[] LISTA_BRANCA = {
            "/login",
            "/refresh-token",
            "/esqueci-minha-senha",
            "/trocar-senha",
            "/actuator/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(exc -> exc.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                    .requestMatchers(LISTA_BRANCA).permitAll()
                    .anyRequest().authenticated());
        http.addFilterBefore(authenticationJwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
