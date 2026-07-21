package com.mib.backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting em memoria (token bucket via Bucket4j, gratuito/open source) aplicado
 * apenas as rotas de autenticacao, para mitigar ataques de forca bruta em login/registro.
 * Em ambiente com multiplas instancias, recomenda-se migrar o estado do bucket para um
 * backend compartilhado gratuito (ex.: Redis) mantendo a mesma API do Bucket4j.
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int CAPACITY = 10;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Muitas tentativas. Aguarde um momento antes de tentar novamente.\"}");
        }
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(CAPACITY,
                io.github.bucket4j.Refill.greedy(CAPACITY, REFILL_PERIOD));
        return Bucket.builder().addLimit(limit).build();
    }

    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        return (forwardedFor != null && !forwardedFor.isBlank())
                ? forwardedFor.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
