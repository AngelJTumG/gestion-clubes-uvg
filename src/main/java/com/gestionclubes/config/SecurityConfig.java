package com.gestionclubes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

/**
 * Configura el cifrado de contraseñas y el acceso a las rutas de la aplicación.
 */
@Configuration
public class SecurityConfig {
        /** Proporciona el codificador BCrypt usado para almacenar y verificar contraseñas. */
        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /** Define permisos por ruta, inicio de sesión con formulario y cierre de sesión. */
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/registro", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin/clubes/{id:[0-9]+}").authenticated()
                        .requestMatchers("/admin/clubes/nuevo", "/admin/clubes").hasRole("ADMIN")
                        .requestMatchers("/admin/clubes/*/editar", "/admin/clubes/*/activar",
                                "/admin/clubes/*/desactivar").hasAnyRole("ADMIN_CLUB", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/club-admin/**").hasAnyRole("ADMIN_CLUB", "ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/panel", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .build();
        }
}