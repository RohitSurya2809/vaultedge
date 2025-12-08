package com.rohitsurya2809.vaultedge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (token == null) {
            log.debug("No Bearer token found in request: {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // quick header inspect
            String username = jwtUtil.extractUsername(token);
            log.debug("JWT token present. subject={}, uri={}", username, request.getRequestURI());

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (userDetails == null) {
                    log.debug("UserDetailsService returned null for username={}", username);
                } else {
                    boolean valid = jwtUtil.validateToken(token);
                    if (!valid) {
                        log.debug("JWT validation failed for user={}", username);
                    } else {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("JWT authentication successful for user={} authorities={}", username, userDetails.getAuthorities());
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("JWT authentication processing failed: {}", ex.toString());
        }

        filterChain.doFilter(request, response);
    }
}
