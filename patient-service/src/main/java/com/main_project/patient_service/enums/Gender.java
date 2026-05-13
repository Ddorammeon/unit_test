package com.main_project.patient_service.enums;

/**
 * Gender Enum
 *
 * Represents the gender of a patient.
 */
public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ"),
    OTHER("Khác");

    private final String vietnameseName;

    Gender(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    /**
     * Parse from Vietnamese name or English name
     */
    public static Gender fromString(String value) {
        if (value == null) {
            return null;
        }

        // Try to match by enum name first
        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }

        // Try to match by Vietnamese name
        for (Gender gender : Gender.values()) {
            if (gender.vietnameseName.equalsIgnoreCase(value)) {
                return gender;
            }
        }

        throw new IllegalArgumentException("Unknown gender value: " + value);
    }
}
