package com.signflow.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
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

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiterAspect {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Around("@annotation(io.github.resilience4j.ratelimiter.annotation.RateLimiter)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String key;

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            key = "user:" + authentication.getName();
        } else {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            key = "ip:" + getClientIp(request);
        }

        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(key, "userRateLimiter");

        if (rateLimiter.acquirePermission()) {
            return joinPoint.proceed();
        } else {
            log.warn("Rate limit excedido para a chave: {}", key);
            throw RequestNotPermitted.createRequestNotPermitted(rateLimiter);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
