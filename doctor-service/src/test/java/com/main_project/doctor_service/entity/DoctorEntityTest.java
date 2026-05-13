package com.main_project.doctor_service.entity;

import com.main_project.doctor_service.enums.SpecializationCodeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorEntityTest {

    @Test
    @DisplayName("DOC-ENT-UT-001 - addDegree should create bidirectional degree")
    void addDegreeShouldCreateDegreeAndAttachToDoctor() {
        // Note: DOC-ENT-UT-001 | Objective: ensure addDegree creates a degree and maintains the doctor-degree association.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .build();

        DoctorDegree degree = doctor.addDegree("DDS", "University A", 2020);

        assertThat(doctor.getDegrees()).hasSize(1);
        assertThat(degree.getDoctor()).isSameAs(doctor);
        assertThat(degree.getDegreeName()).isEqualTo("DDS");
    }

    @Test
    @DisplayName("DOC-ENT-UT-002 - removeDegree should detach degree from doctor")
    void removeDegreeShouldRemoveDegreeAndClearBackReference() {
        // Note: DOC-ENT-UT-002 | Objective: ensure removeDegree removes the degree from the list and clears its doctor reference.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .build();
        DoctorDegree degree = doctor.addDegree("DDS", "University A", 2020);

        doctor.removeDegree(degree);

        assertThat(doctor.getDegrees()).isEmpty();
        assertThat(degree.getDoctor()).isNull();
    }

    @Test
    @DisplayName("DOC-ENT-UT-003 - removeDegreeById should return true for existing degree")
    void removeDegreeByIdShouldReturnTrueAndRemoveDegreeWhenIdExists() {
        // Note: DOC-ENT-UT-003 | Objective: ensure removeDegreeById returns true and removes the matching degree from the aggregate.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .build();
        DoctorDegree degree = doctor.addDegree("DDS", "University A", 2020);
        degree.setId(UUID.randomUUID());

        boolean removed = doctor.removeDegreeById(degree.getId());

        assertThat(removed).isTrue();
        assertThat(doctor.getDegrees()).isEmpty();
    }

    @Test
    @DisplayName("DOC-ENT-UT-004 - removeDegreeById should return false for missing degree")
    void removeDegreeByIdShouldReturnFalseWhenIdDoesNotExist() {
        // Note: DOC-ENT-UT-004 | Objective: ensure removeDegreeById returns false when no degree matches the requested id.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .build();
        DoctorDegree degree = doctor.addDegree("DDS", "University A", 2020);
        degree.setId(UUID.randomUUID());

        boolean removed = doctor.removeDegreeById(UUID.randomUUID());

        assertThat(removed).isFalse();
        assertThat(doctor.getDegrees()).hasSize(1);
    }

    @Test
    @DisplayName("DOC-ENT-UT-005 - updateDegrees should replace existing degrees")
    void updateDegreesShouldReplaceExistingDegrees() {
        // Note: DOC-ENT-UT-005 | Objective: ensure updateDegrees removes old degrees and replaces them with the new degree list.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .build();
        doctor.addDegree("Old", "University A", 2018);

        doctor.updateDegrees(List.of(
                new Doctor.DegreeData("DDS", "University B", 2020),
                new Doctor.DegreeData("MDS", "University C", 2022)
        ));

        assertThat(doctor.getDegrees()).hasSize(2);
        assertThat(doctor.getDegrees())
                .extracting(DoctorDegree::getDegreeName)
                .containsExactly("DDS", "MDS");
        assertThat(doctor.getDegrees()).allMatch(item -> item.getDoctor() == doctor);
    }

    @Test
    @DisplayName("DOC-ENT-UT-006 - updateDegrees with null should clear degrees")
    void updateDegreesShouldClearDegreesWhenNewListIsNull() {
        // Note: DOC-ENT-UT-006 | Objective: ensure updateDegrees clears all degrees when the replacement list is null.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .build();
        doctor.addDegree("DDS", "University A", 2020);

        doctor.updateDegrees(null);

        assertThat(doctor.getDegrees()).isEmpty();
    }

    @Test
    @DisplayName("DOC-ENT-UT-007 - clearDegrees should remove all degrees")
    void clearDegreesShouldRemoveAllDegrees() {
        // Note: DOC-ENT-UT-007 | Objective: ensure clearDegrees empties the aggregate degree collection completely.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .build();
        doctor.addDegree("DDS", "University A", 2020);
        doctor.addDegree("MDS", "University B", 2022);

        doctor.clearDegrees();

        assertThat(doctor.getDegrees()).isEmpty();
    }

    @Test
    @DisplayName("DOC-ENT-UT-008 - updateBasicInfo should replace business fields")
    void updateBasicInfoShouldReplaceDoctorFields() {
        // Note: DOC-ENT-UT-008 | Objective: ensure updateBasicInfo updates specialization, hospital, license, and consultation fee.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.GEN)
                .workingHospital("Hospital A")
                .licenseNumber("OLD")
                .consultationFeeAmount(100000)
                .build();

        doctor.updateBasicInfo(SpecializationCodeEnum.ORTHO, "Hospital B", "NEW", 250000);

        assertThat(doctor.getSpecializationCode()).isEqualTo(SpecializationCodeEnum.ORTHO);
        assertThat(doctor.getWorkingHospital()).isEqualTo("Hospital B");
        assertThat(doctor.getLicenseNumber()).isEqualTo("NEW");
        assertThat(doctor.getConsultationFeeAmount()).isEqualTo(250000);
    }

    @Test
    @DisplayName("DOC-ENT-UT-009 - removeDegreeById should clear bidirectional link when degree is removed")
    void removeDegreeByIdShouldClearBidirectionalLinkWhenDegreeIsRemoved() {
        // Note: DOC-ENT-UT-009 | Objective: ensure a removed degree is detached from doctor to keep bidirectional consistency.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .degrees(new ArrayList<>())
                .build();
        DoctorDegree degree = doctor.addDegree("DDS", "Medical University", 2020);
        degree.setId(UUID.randomUUID());

        boolean removed = doctor.removeDegreeById(degree.getId());

        assertThat(removed).isTrue();
        assertThat(doctor.getDegrees()).isEmpty();
        assertThat(degree.getDoctor()).isNull();
    }

    @Test
    @DisplayName("DOC-ENT-UT-010 - clearDegrees should clear bidirectional links for all degrees")
    void clearDegreesShouldClearBidirectionalLinksForAllDegrees() {
        // Note: DOC-ENT-UT-010 | Objective: ensure clearing degrees also clears the back-reference on each removed degree.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .degrees(new ArrayList<>())
                .build();
        DoctorDegree firstDegree = doctor.addDegree("DDS", "Medical University", 2020);
        DoctorDegree secondDegree = doctor.addDegree("MS", "Health Academy", 2022);

        doctor.clearDegrees();

        assertThat(doctor.getDegrees()).isEmpty();
        assertThat(firstDegree.getDoctor()).isNull();
        assertThat(secondDegree.getDoctor()).isNull();
    }
}
