package com.school.waimai.common.config;

import com.school.waimai.auth.filter.AdminJwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        AdminJwtFilter adminJwtFilter) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // 管理端登录接口、文档等放行
                                                .requestMatchers(
                                                                "/api/admin/auth/login",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                // 其它请求目前全部放行，实际权限控制交给 AdminJwtFilter
                                                .anyRequest().permitAll())
                                .httpBasic(Customizer.withDefaults())
                                .formLogin(form -> form.disable())
                                .addFilterBefore(adminJwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
