package com.main_project.doctor_service.util;

import com.main_project.doctor_service.dto.DoctorDegreeRequestDTO;
import com.main_project.doctor_service.dto.DoctorRequestDTO;
import com.main_project.doctor_service.dto.DoctorWorkScheduleRequestDTO;
import com.main_project.doctor_service.dto.WorkScheduleRequestDTO;
import com.main_project.doctor_service.entity.Doctor;
import com.main_project.doctor_service.entity.DoctorDegree;
import com.main_project.doctor_service.entity.DoctorWorkSchedule;
import com.main_project.doctor_service.entity.WorkSchedule;
import com.main_project.doctor_service.enums.SpecializationCodeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMapperTest {

    private final EntityMapper mapper = new EntityMapper();

    @Test
    @DisplayName("MAP-UT-001 - toDoctorEntity should map doctor and nested degrees")
    void toDoctorEntityShouldMapDoctorAndNestedDegrees() {
        // Note: MAP-UT-001 | Objective: ensure toDoctorEntity maps doctor fields and creates nested degrees from the request.
        UUID doctorId = UUID.randomUUID();
        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setUserId(doctorId);
        request.setSpecializationCode(SpecializationCodeEnum.ORTHO);
        request.setWorkingHospital("Hospital A");
        request.setLicenseNumber("LIC-001");
        request.setConsultationFeeAmount(500000);

        DoctorDegreeRequestDTO degreeRequest = new DoctorDegreeRequestDTO();
        degreeRequest.setDoctorId(doctorId);
        degreeRequest.setDegreeName("DDS");
        degreeRequest.setInstitution("University A");
        degreeRequest.setYearObtained(2020);
        request.setDegrees(List.of(degreeRequest));

        Doctor doctor = mapper.toDoctorEntity(request);

        assertThat(doctor.getUserId()).isEqualTo(doctorId);
        assertThat(doctor.getSpecializationCode()).isEqualTo(SpecializationCodeEnum.ORTHO);
        assertThat(doctor.getDegrees()).hasSize(1);
        assertThat(doctor.getDegrees().get(0).getDoctor()).isSameAs(doctor);
    }

    @Test
    @DisplayName("MAP-UT-002 - updateDoctorEntity should replace degrees and basic info")
    void updateDoctorEntityShouldReplaceDoctorFieldsAndDegrees() {
        // Note: MAP-UT-002 | Objective: ensure updateDoctorEntity updates basic fields and fully replaces existing degrees.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.GEN)
                .workingHospital("Old Hospital")
                .licenseNumber("OLD")
                .consultationFeeAmount(100000)
                .build();
        doctor.addDegree("Old", "Old University", 2018);

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setUserId(doctor.getUserId());
        request.setSpecializationCode(SpecializationCodeEnum.ORTHO);
        request.setWorkingHospital("New Hospital");
        request.setLicenseNumber("NEW");
        request.setConsultationFeeAmount(300000);
        DoctorDegreeRequestDTO degreeRequest = new DoctorDegreeRequestDTO();
        degreeRequest.setDoctorId(doctor.getUserId());
        degreeRequest.setDegreeName("DDS");
        degreeRequest.setInstitution("New University");
        degreeRequest.setYearObtained(2021);
        request.setDegrees(List.of(degreeRequest));

        mapper.updateDoctorEntity(doctor, request);

        assertThat(doctor.getSpecializationCode()).isEqualTo(SpecializationCodeEnum.ORTHO);
        assertThat(doctor.getWorkingHospital()).isEqualTo("New Hospital");
        assertThat(doctor.getDegrees()).hasSize(1);
        assertThat(doctor.getDegrees().get(0).getDegreeName()).isEqualTo("DDS");
    }

    @Test
    @DisplayName("MAP-UT-003 - updateDoctorEntity with null degrees should clear degrees")
    void updateDoctorEntityShouldClearDegreesWhenRequestDegreesIsNull() {
        // Note: MAP-UT-003 | Objective: ensure updateDoctorEntity clears all doctor degrees when the request degree list is null.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.GEN)
                .build();
        doctor.addDegree("DDS", "University A", 2020);

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setUserId(doctor.getUserId());
        request.setSpecializationCode(SpecializationCodeEnum.ORTHO);
        request.setWorkingHospital("Hospital A");
        request.setLicenseNumber("LIC-001");
        request.setConsultationFeeAmount(200000);
        request.setDegrees(null);

        mapper.updateDoctorEntity(doctor, request);

        assertThat(doctor.getDegrees()).isEmpty();
    }

    @Test
    @DisplayName("MAP-UT-004 - toDoctorResponse should map nested degrees")
    void toDoctorResponseShouldMapDoctorAndDegreeResponses() {
        // Note: MAP-UT-004 | Objective: ensure toDoctorResponse maps doctor fields and nested degree response DTOs correctly.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .workingHospital("Hospital A")
                .licenseNumber("LIC-001")
                .consultationFeeAmount(500000)
                .build();
        DoctorDegree degree = doctor.addDegree("DDS", "University A", 2020);
        degree.setId(UUID.randomUUID());

        var response = mapper.toDoctorResponse(doctor);

        assertThat(response.getUserId()).isEqualTo(doctor.getUserId());
        assertThat(response.getDegrees()).hasSize(1);
        assertThat(response.getDegrees().get(0).getDegreeName()).isEqualTo("DDS");
    }

    @Test
    @DisplayName("MAP-UT-005 - toWorkScheduleEntity should map schedule fields")
    void toWorkScheduleEntityShouldMapScheduleFields() {
        // Note: MAP-UT-005 | Objective: ensure toWorkScheduleEntity maps work date and time fields from request to entity.
        WorkScheduleRequestDTO request = new WorkScheduleRequestDTO();
        request.setWorkDate(LocalDate.of(2026, 5, 13));
        request.setStartTime(ZonedDateTime.of(2026, 5, 13, 8, 0, 0, 0, ZoneOffset.UTC));
        request.setEndTime(ZonedDateTime.of(2026, 5, 13, 17, 0, 0, 0, ZoneOffset.UTC));

        WorkSchedule entity = mapper.toWorkScheduleEntity(request);

        assertThat(entity.getWorkDate()).isEqualTo(request.getWorkDate());
        assertThat(entity.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(entity.getEndTime()).isEqualTo(request.getEndTime());
    }

    @Test
    @DisplayName("MAP-UT-006 - updateWorkScheduleEntity should overwrite schedule fields")
    void updateWorkScheduleEntityShouldOverwriteFields() {
        // Note: MAP-UT-006 | Objective: ensure updateWorkScheduleEntity overwrites an existing schedule with request values.
        WorkSchedule entity = WorkSchedule.builder().build();
        WorkScheduleRequestDTO request = new WorkScheduleRequestDTO();
        request.setWorkDate(LocalDate.of(2026, 5, 13));
        request.setStartTime(ZonedDateTime.of(2026, 5, 13, 8, 0, 0, 0, ZoneOffset.UTC));
        request.setEndTime(ZonedDateTime.of(2026, 5, 13, 17, 0, 0, 0, ZoneOffset.UTC));

        mapper.updateWorkScheduleEntity(entity, request);

        assertThat(entity.getWorkDate()).isEqualTo(request.getWorkDate());
        assertThat(entity.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(entity.getEndTime()).isEqualTo(request.getEndTime());
    }

    @Test
    @DisplayName("MAP-UT-007 - toDoctorWorkScheduleEntity should map doctor and schedule links")
    void toDoctorWorkScheduleEntityShouldMapAssociations() {
        // Note: MAP-UT-007 | Objective: ensure toDoctorWorkScheduleEntity maps status and both linked aggregate references.
        Doctor doctor = Doctor.builder().userId(UUID.randomUUID()).specializationCode(SpecializationCodeEnum.ORTHO).build();
        WorkSchedule schedule = WorkSchedule.builder().id(UUID.randomUUID()).build();
        DoctorWorkScheduleRequestDTO request = new DoctorWorkScheduleRequestDTO();
        request.setDoctorId(doctor.getUserId());
        request.setWorkScheduleId(schedule.getId());
        request.setStatus("ACTIVE");

        DoctorWorkSchedule entity = mapper.toDoctorWorkScheduleEntity(request, doctor, schedule);

        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getDoctor()).isSameAs(doctor);
        assertThat(entity.getWorkSchedule()).isSameAs(schedule);
    }

    @Test
    @DisplayName("MAP-UT-008 - updateDoctorWorkScheduleEntity should overwrite associations")
    void updateDoctorWorkScheduleEntityShouldOverwriteAssociations() {
        // Note: MAP-UT-008 | Objective: ensure updateDoctorWorkScheduleEntity replaces status, doctor, and work schedule references.
        DoctorWorkSchedule entity = DoctorWorkSchedule.builder().status("OLD").build();
        Doctor doctor = Doctor.builder().userId(UUID.randomUUID()).specializationCode(SpecializationCodeEnum.ORTHO).build();
        WorkSchedule schedule = WorkSchedule.builder().id(UUID.randomUUID()).build();
        DoctorWorkScheduleRequestDTO request = new DoctorWorkScheduleRequestDTO();
        request.setDoctorId(doctor.getUserId());
        request.setWorkScheduleId(schedule.getId());
        request.setStatus("ACTIVE");

        mapper.updateDoctorWorkScheduleEntity(entity, request, doctor, schedule);

        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getDoctor()).isSameAs(doctor);
        assertThat(entity.getWorkSchedule()).isSameAs(schedule);
    }

    @Test
    @DisplayName("MAP-UT-009 - null inputs should return null or keep target unchanged")
    void nullInputsShouldBeHandledSafely() {
        // Note: MAP-UT-009 | Objective: ensure mapper handles null inputs safely without throwing unexpected exceptions.
        WorkSchedule entity = WorkSchedule.builder().workDate(LocalDate.of(2026, 1, 1)).build();

        assertThat(mapper.toDoctorEntity(null)).isNull();
        assertThat(mapper.toDoctorResponse(null)).isNull();
        assertThat(mapper.toDoctorDegreeEntity(null, null)).isNull();
        assertThat(mapper.toWorkScheduleEntity(null)).isNull();
        assertThat(mapper.toDoctorWorkScheduleEntity(null, null, null)).isNull();

        mapper.updateWorkScheduleEntity(entity, null);

        assertThat(entity.getWorkDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("MAP-UT-010 - updateDoctorEntity should preserve existing degrees when request omits degree list")
    void updateDoctorEntityShouldPreserveExistingDegreesWhenRequestOmitsDegreeList() {
        // Note: MAP-UT-010 | Objective: ensure omitted degree list keeps existing degrees instead of clearing them unexpectedly.
        Doctor doctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .degrees(new ArrayList<>())
                .build();
        doctor.addDegree("DDS", "Medical University", 2020);

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setUserId(doctor.getUserId());
        request.setSpecializationCode(SpecializationCodeEnum.ENDO);
        request.setWorkingHospital("Updated Hospital");
        request.setLicenseNumber("LIC-NEW");
        request.setConsultationFeeAmount(700000);
        request.setDegrees(null);

        mapper.updateDoctorEntity(doctor, request);

        assertThat(doctor.getDegrees()).hasSize(1);
    }
}
