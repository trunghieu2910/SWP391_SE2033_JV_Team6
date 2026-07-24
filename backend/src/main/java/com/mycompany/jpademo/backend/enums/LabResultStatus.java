package com.mycompany.jpademo.backend.enums;

/**
 * Lifecycle status of a LabResult.
 * PENDING   — order created, awaiting result values.
 * COMPLETED — result values received and saved; cannot be edited/deleted afterwards.
 * CANCELED  — order cancelled before results arrived (currently unused by any flow).
 */
public enum LabResultStatus {
    PENDING,
    COMPLETED
}
