package com.mib.backend.security;

import com.mib.backend.service.PresenceService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final PresenceService presenceService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HEADER_NAME);

        if (header == null || !header.startsWith(PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length());

        try {
            if (jwtService.isTokenValid(token)) {
                UUID userId = jwtService.extractUserId(token);
                UserDetails userDetails = userDetailsService.loadUserById(userId);

                if (userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    presenceService.touch(userId);
                }
            }
        } catch (Exception ex) {
            log.debug("Falha ao autenticar via JWT: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
