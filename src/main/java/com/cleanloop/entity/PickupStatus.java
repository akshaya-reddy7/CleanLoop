package com.cleanloop.entity;

/**
 * Lifecycle statuses for waste pickup requests.
 */
public enum PickupStatus {
    REQUESTED,
    APPROVED,
    SCHEDULED,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    REJECTED
}
