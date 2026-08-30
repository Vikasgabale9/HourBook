package com.app.hourbook.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {

		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {

			filterChain.doFilter(request, response);

			return;
		}

		String token = authHeader.substring(7);

		System.out.println("JWT FILTER HIT");

		if (jwtService.isTokenValid(token)) {

			Long lid = jwtService.extractId(token);
			
			Integer id =lid.intValue();

			String identifier = jwtService.extractIdentifier(token);

			String entity = jwtService.extractEntity(token);

			String role = jwtService.extractRole(token);

			AuthenticatedPrincipal principal =
			        new AuthenticatedPrincipal(
			                id,
			                identifier,
			                entity,
			                role
			        );

			UsernamePasswordAuthenticationToken authentication =
			        new UsernamePasswordAuthenticationToken(
			                principal,
			                null,
			                List.of(new SimpleGrantedAuthority(role)));

			SecurityContextHolder.getContext().setAuthentication(authentication);

			System.out.println("AUTH = " + SecurityContextHolder.getContext().getAuthentication());
			
		}

		filterChain.doFilter(request, response);
	}
}