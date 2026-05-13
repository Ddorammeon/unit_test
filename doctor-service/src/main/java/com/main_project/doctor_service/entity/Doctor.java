package com.main_project.doctor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.main_project.doctor_service.enums.SpecializationCodeEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Doctor Aggregate Root (DDD)
 *
 * This is the Aggregate Root for the Doctor aggregate.
 * All operations on DoctorDegree must go through this aggregate root.
 * Direct manipulation of DoctorDegree through a separate repository is NOT allowed.
 */
@Entity
@Table(name = "doctor")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @Column(name = "user_id", length = 50, nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialization_code", length = 50, nullable = false)
    private SpecializationCodeEnum specializationCode;

    @Column(name = "working_hospital", length = 255)
    private String workingHospital;

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "consultation_fee_amount")
    private Integer consultationFeeAmount;

    @Builder.Default
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoctorDegree> degrees = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoctorWorkSchedule> doctorWorkSchedules = new ArrayList<>();

    // ========== Aggregate Root Business Methods ==========

    /**
     * Adds a degree to this doctor.
     * This method maintains the bidirectional relationship.
     *
     * @param degreeName the name of the degree
     * @param institution the institution where the degree was obtained
     * @param yearObtained the year the degree was obtained
     * @return the newly created DoctorDegree
     */
    public DoctorDegree addDegree(String degreeName, String institution, Integer yearObtained) {
        DoctorDegree degree = DoctorDegree.builder()
                .degreeName(degreeName)
                .institution(institution)
                .yearObtained(yearObtained)
                .doctor(this)
                .build();
        this.degrees.add(degree);
        return degree;
    }

    /**
     * Removes a degree from this doctor.
     * Due to orphanRemoval=true, the degree will be deleted from database.
     *
     * @param degree the degree to remove
     */
    public void removeDegree(DoctorDegree degree) {
        this.degrees.remove(degree);
        degree.setDoctor(null);
    }

    /**
     * Removes a degree by its ID.
     *
     * @param degreeId the ID of the degree to remove
     * @return true if the degree was found and removed, false otherwise
     */
    public boolean removeDegreeById(UUID degreeId) {
        return this.degrees.removeIf(degree -> degree.getId().equals(degreeId));
    }

    /**
     * Updates all degrees by replacing the entire list.
     * Old degrees not in the new list will be removed (orphanRemoval=true).
     *
     * @param newDegrees list of new degree data
     */
    public void updateDegrees(List<DegreeData> newDegrees) {
        // Clear existing degrees (orphanRemoval will delete them)
        this.degrees.clear();

        // Add new degrees
        if (newDegrees != null) {
            newDegrees.forEach(data ->
                addDegree(data.getDegreeName(), data.getInstitution(), data.getYearObtained())
            );
        }
    }

    /**
     * Clears all degrees from this doctor.
     * Due to orphanRemoval=true, all degrees will be deleted from database.
     */
    public void clearDegrees() {
        this.degrees.clear();
    }

    /**
     * Updates the doctor's basic information.
     *
     * @param specializationCode the specialization code
     * @param workingHospital the working hospital
     * @param licenseNumber the license number
     * @param consultationFeeAmount the consultation fee amount
     */
    public void updateBasicInfo(SpecializationCodeEnum specializationCode,
                                 String workingHospital,
                                 String licenseNumber,
                                 Integer consultationFeeAmount) {
        this.specializationCode = specializationCode;
        this.workingHospital = workingHospital;
        this.licenseNumber = licenseNumber;
        this.consultationFeeAmount = consultationFeeAmount;
    }

    // ========== Helper Classes ==========

    /**
     * Value object for degree data transfer
     */
    @Getter
    @AllArgsConstructor
    public static class DegreeData {
        private String degreeName;
        private String institution;
        private Integer yearObtained;
    }
}
