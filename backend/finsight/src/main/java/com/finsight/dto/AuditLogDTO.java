package com.finsight.dto;

import com.finsight.model.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private Long userId;
    private String action;
    private String entityType;
    private Long entityId;
    private LocalDateTime createdAt;

    public static AuditLogDTO from(AuditLog a) {
        return new AuditLogDTO(a.getId(), a.getUserId(), a.getAction(), a.getEntityType(),
                a.getEntityId(), a.getCreatedAt());
    }
}
