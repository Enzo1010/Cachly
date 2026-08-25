package br.com.cachly.backend.seguranca;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@Profile("!test")
public class RateLimitingFilter extends OncePerRequestFilter {

    // Utiliza expireAfterAccess superior à maior janela de rate limit (1 min) para evitar recriar buckets ativos.
    // Expiração de 15 minutos é segura. maximumSize limita picos extremos de tráfego.
    private final Cache<String, Bucket> loginBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .build();

    private final Cache<String, Bucket> simuladorBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .build();

    private final Cache<String, Bucket> apiBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = getClientIP(request);

        Bucket bucket = null;

        if (path.startsWith("/api/auth/login")) {
            bucket = loginBuckets.get(ip, this::createNewLoginBucket);
        } else if (path.startsWith("/api/simulador/executar")) {
            bucket = simuladorBuckets.get(ip, this::createNewSimuladorBucket);
        } else if (path.startsWith("/api/")) {
            bucket = apiBuckets.get(ip, this::createNewApiBucket);
        }

        if (bucket != null) {
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Muitas requisicoes. Tente novamente mais tarde.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private Bucket createNewLoginBucket(String key) {
        Bandwidth limit = Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofMinutes(1)).build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createNewSimuladorBucket(String key) {
        Bandwidth limit = Bandwidth.builder().capacity(1).refillIntervally(1, Duration.ofSeconds(10)).build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createNewApiBucket(String key) {
        Bandwidth limit = Bandwidth.builder().capacity(100).refillIntervally(100, Duration.ofMinutes(1)).build();
        return Bucket.builder().addLimit(limit).build();
    }
}
