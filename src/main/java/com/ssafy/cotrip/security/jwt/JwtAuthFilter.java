package com.ssafy.cotrip.security.jwt;

import com.ssafy.cotrip.security.service.CustomUserDetails;
import com.ssafy.cotrip.security.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        log.info("doFilterInternal 진입");
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/api/v1/auth/") || requestURI.startsWith("/api/v1/attractions/") ||
        requestURI.startsWith("/api/v1/images")) {
            log.info("스킵성공");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtTokenUtil.isTokenExpired(token)) {
                throw new ExpiredJwtException(null, null, "토큰 만료");
            }
        } catch (ExpiredJwtException e) {
            request.setAttribute("jwt_exception", "EXPIRED");
            throw new AuthenticationCredentialsNotFoundException("JWT 만료");
        }

        String email = jwtTokenUtil.extractUsername(token);

        if (email != null) {
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

            if (jwtTokenUtil.validateToken(token, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}