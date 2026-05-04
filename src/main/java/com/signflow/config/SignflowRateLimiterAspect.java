package com.signflow.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Slf4j
@Aspect
@Component("signflowRateLimiterAspect")
@RequiredArgsConstructor
public class SignflowRateLimiterAspect {

    private final RateLimiterRegistry rateLimiterRegistry;

    // Configuração padrão — 10 requisições por minuto
    private static final RateLimiterConfig DEFAULT_CONFIG = RateLimiterConfig.custom()
            .limitForPeriod(10)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ZERO)
            .build();

    @Around("@annotation(io.github.resilience4j.ratelimiter.annotation.RateLimiter)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        String key = resolveKey();

        // Busca instância existente ou cria com config padrão — nunca lança ConfigNotFoundException
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(key, DEFAULT_CONFIG);

        if (rateLimiter.acquirePermission()) {
            return joinPoint.proceed();
        }

        log.warn("Rate limit excedido para a chave: {}", key);
        throw RequestNotPermitted.createRequestNotPermitted(rateLimiter);
    }

    private String resolveKey() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return "user:" + auth.getName();
        }
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return "ip:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}
