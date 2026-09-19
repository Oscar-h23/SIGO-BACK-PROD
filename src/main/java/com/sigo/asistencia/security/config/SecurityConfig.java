package com.sigo.asistencia.security.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/error").permitAll()

                        .requestMatchers("/api/programacion/mi-horario")
                            .hasAnyRole("SUPERVISOR", "CONTROLADOR", "OPERADOR")
                        .requestMatchers("/api/programacion/grupos/**")
                            .hasRole("SUPERVISOR")
                        .requestMatchers(HttpMethod.GET, "/api/programacion/turnos")
                            .hasAnyRole("SUPERVISOR", "CONTROLADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/programacion/turnos")
                            .hasRole("SUPERVISOR")
                        .requestMatchers("/api/distribucion/**")
                            .hasAnyRole("SUPERVISOR", "CONTROLADOR")

                        .requestMatchers("/api/dashboard/**").hasAnyRole("SUPERVISOR", "CONTROLADOR")
                        .requestMatchers("/api/asistencias/**").hasAnyRole("SUPERVISOR", "CONTROLADOR")
                        .requestMatchers("/api/trabajadores/**").hasAnyRole("SUPERVISOR", "CONTROLADOR")
                        .requestMatchers("/api/chat/**").hasAnyRole("SUPERVISOR", "CONTROLADOR")
                        .requestMatchers("/api/inventario/productos/**").hasAnyRole("SUPERVISOR", "CONTROLADOR")
                        .requestMatchers("/api/inventario/catalogos/**").hasAnyRole("SUPERVISOR", "CONTROLADOR")
                        .requestMatchers("/api/relevos/**", "/api/vias/**")
                            .hasAnyRole("SUPERVISOR", "CONTROLADOR", "OPERADOR")
                        .requestMatchers("/api/inventarios/**", "/api/inventario/me", "/api/inventario/stock/**")
                            .hasAnyRole("SUPERVISOR", "CONTROLADOR", "OPERADOR")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(token -> {
                            String rol = token.getClaimAsString("rol");
                            var authorities = rol == null
                                    ? List.<SimpleGrantedAuthority>of()
                                    : List.of(new SimpleGrantedAuthority("ROLE_" + rol));
                            return new JwtAuthenticationToken(token, authorities, token.getSubject());
                        })
                ))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes para HS256");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey key, @Value("${app.jwt.issuer:sigo-api}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}
