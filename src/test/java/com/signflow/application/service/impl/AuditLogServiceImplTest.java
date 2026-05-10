package com.signflow.application.service.impl;

import com.signflow.infrastructure.persistence.entity.AuditLogEntity;
import com.signflow.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldLogActionWithAuthenticatedUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla");

        auditLogService.log("ACTION", "RESOURCE", "ID", "DETAILS");

        verify(auditLogRepository, times(1)).save(any(AuditLogEntity.class));
    }

    @Test
    void shouldLogActionWithAnonymousUser() {
        when(securityContext.getAuthentication()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla");

        auditLogService.log("ACTION", "RESOURCE", "ID", "DETAILS");

        verify(auditLogRepository, times(1)).save(argThat(log -> "ANONYMOUS".equals(log.getUserId())));
    }

    @Test
    void shouldLogWithExplicitParameters() {
        auditLogService.log("user1", "ACTION", "RESOURCE", "ID", "DETAILS", "10.0.0.1", "Agent");

        verify(auditLogRepository, times(1)).save(argThat(log -> 
            "user1".equals(log.getUserId()) &&
            "10.0.0.1".equals(log.getIpAddress()) &&
            "Agent".equals(log.getUserAgent())
        ));
    }
}
