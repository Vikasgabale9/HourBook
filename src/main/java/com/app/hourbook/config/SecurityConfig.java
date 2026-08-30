package com.app.hourbook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.app.hourbook.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtFilter) {

		this.jwtFilter = jwtFilter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())

				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/admin/**",
								"/vendor/**",
								"/user/**",
								"/css/**",
								"/js/**",
								"/images/**",
								"/uploads/**",
								"/favicon.ico"
								)
						.permitAll()

						.requestMatchers("/api/vendor/register",
								"/api/vendor/verify-otp",
								"/api/user/register",
								"/api/user/verify-otp")
						.permitAll()  

						.requestMatchers("/api/vendor/**").hasRole("VENDOR")
					.requestMatchers("/api/vendorDetails/**").hasRole("VENDOR")


						.requestMatchers("/api/user/**").hasRole("USER")
						.requestMatchers("/api/appUserDetails/**").hasRole("USER")
						.anyRequest().authenticated())

				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}