package com.signflow.application.service.impl;

import com.signflow.application.service.AuditLogService;
import com.signflow.infrastructure.persistence.entity.AuditLogEntity;
import com.signflow.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    @Async
    @Override
    public void log(String action, String resourceType, String resourceId, String details) {
        try {
            String userId = getAuthenticatedUserId();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            saveLog(userId, action, resourceType, resourceId, details, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("Erro ao registrar log de auditoria: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void log(String userId, String action, String resourceType, String resourceId, String details, String ipAddress, String userAgent) {
        try {
            saveLog(userId, action, resourceType, resourceId, details, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("Erro ao registrar log de auditoria: {}", e.getMessage());
        }
    }

    private void saveLog(String userId, String action, String resourceType, String resourceId, String details, String ipAddress, String userAgent) {
        AuditLogEntity auditLog = AuditLogEntity.builder()
                .userId(userId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.debug("[AUDIT] {} - {} - {}: {}", userId, action, resourceType, details);
    }

    private String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "ANONYMOUS";
    }
}
