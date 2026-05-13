package com.main_project.patient_service.enums;

/**
 * BloodType Enum
 *
 * Represents the blood type of a patient following the ABO and Rh system.
 */
public enum BloodType {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    UNKNOWN("Unknown");

    private final String displayName;

    BloodType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parse from display name (e.g., "A+", "O-") or enum name
     */
    public static BloodType fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }

        // Try to match by enum name first
        for (BloodType bloodType : BloodType.values()) {
            if (bloodType.name().equalsIgnoreCase(value)) {
                return bloodType;
            }
        }

        // Try to match by display name
        for (BloodType bloodType : BloodType.values()) {
            if (bloodType.displayName.equalsIgnoreCase(value)) {
                return bloodType;
            }
        }

        return UNKNOWN;
    }
}
