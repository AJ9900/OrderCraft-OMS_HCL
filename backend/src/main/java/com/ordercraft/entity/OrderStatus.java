package com.ordercraft.entity;

public enum OrderStatus {
    DRAFT,
    CONFIRMED,
    MATERIAL_CHECK,
    MATERIAL_SHORTAGE,
    READY_FOR_PRODUCTION,
    IN_PRODUCTION,
    QUALITY_CHECK,
    COMPLETED,
    CANCELLED
}
