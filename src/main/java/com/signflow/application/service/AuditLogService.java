package com.signflow.application.service;

public interface AuditLogService {
    void log(String action, String resourceType, String resourceId, String details);
    void log(String userId, String action, String resourceType, String resourceId, String details, String ipAddress, String userAgent);
}
