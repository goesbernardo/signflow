package com.signflow.infrastructure.persistence.listener;

import com.signflow.infrastructure.security.TenantContext;
import jakarta.persistence.PrePersist;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class TenantEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            try {
                Field tenantField = findField(entity.getClass(), "tenantId");
                if (tenantField != null) {
                    tenantField.setAccessible(true);
                    if (tenantField.get(entity) == null) {
                        tenantField.set(entity, tenantId);
                        log.debug("TenantId {} definido automaticamente para a entidade {}", tenantId, entity.getClass().getSimpleName());
                    }
                }
            } catch (Exception e) {
                log.error("Erro ao definir tenantId automaticamente na entidade {}: {}", entity.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
