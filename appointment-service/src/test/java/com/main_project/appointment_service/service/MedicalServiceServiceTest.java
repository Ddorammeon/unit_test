package com.main_project.appointment_service.service;

import com.main_project.appointment_service.dto.MedicalServiceDTO;
import com.main_project.appointment_service.dto.MedicalServiceRequestDTO;
import com.main_project.appointment_service.entity.MedicalService;
import com.main_project.appointment_service.enums.MedicalServiceStatus;
import com.main_project.appointment_service.repository.MedicalServiceRepository;
import com.main_project.appointment_service.util.EntityDTOMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalServiceServiceTest {

    @Mock
    private MedicalServiceRepository repository;
    @Mock
    private EntityDTOMapper mapper;

    @InjectMocks
    private MedicalServiceService medicalServiceService;

    @Test
    @DisplayName("MED-SRV-UT-008 - Create medical service successfully")
    void createMedicalServiceShouldSaveAndReturnDto() {
        // Note: MED-SRV-UT-008 | Objective: create a medical service successfully and return the mapped DTO.
        MedicalServiceRequestDTO request = buildMedicalServiceRequest();
        MedicalService entity = buildMedicalService();
        MedicalServiceDTO dto = buildMedicalServiceDto(entity);

        when(mapper.toMedicalServiceEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toMedicalServiceDTO(entity)).thenReturn(dto);

        assertThat(medicalServiceService.createMedicalService(request)).isSameAs(dto);
    }

    @Test
    @DisplayName("MED-SRV-UT-009 - Update medical service successfully")
    void updateMedicalServiceShouldSaveAndReturnDto() {
        // Note: MED-SRV-UT-009 | Objective: update an existing medical service and return the mapped DTO result.
        UUID id = UUID.randomUUID();
        MedicalServiceRequestDTO request = buildMedicalServiceRequest();
        MedicalService entity = buildMedicalService();
        MedicalServiceDTO dto = buildMedicalServiceDto(entity);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toMedicalServiceDTO(entity)).thenReturn(dto);

        assertThat(medicalServiceService.updateMedicalService(id, request)).isSameAs(dto);
        verify(mapper).updateMedicalServiceEntity(entity, request);
    }

    @Test
    @DisplayName("MED-SRV-UT-010 - Reject update when medical service is missing")
    void updateMedicalServiceShouldThrowWhenMissing() {
        // Note: MED-SRV-UT-010 | Objective: throw exception when trying to update a medical service that does not exist.
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalServiceService.updateMedicalService(id, buildMedicalServiceRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Medical Service not found");
    }

    @Test
    @DisplayName("MED-SRV-UT-011 - Deactivate medical service by name successfully")
    void deactivateMedicalServiceNameShouldReturnUpdatedRows() {
        // Note: MED-SRV-UT-011 | Objective: return updated row count when deactivating medical services by exact name.
        when(repository.deactivateMedicalServiceName("X-Ray")).thenReturn(2);

        assertThat(medicalServiceService.deactivateMedicalServiceName("X-Ray")).isEqualTo(2);
    }

    @Test
    @DisplayName("MED-SRV-UT-012 - Reject deactivate by name when nothing is updated")
    void deactivateMedicalServiceNameShouldThrowWhenNothingUpdated() {
        // Note: MED-SRV-UT-012 | Objective: throw exception when no medical service matches the name for deactivation.
        when(repository.deactivateMedicalServiceName("X-Ray")).thenReturn(0);

        assertThatThrownBy(() -> medicalServiceService.deactivateMedicalServiceName("X-Ray"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("MED-SRV-UT-013 - Deactivate medical service by id successfully")
    void deactivateMedicalServiceShouldSetInactiveStatus() {
        // Note: MED-SRV-UT-013 | Objective: set a medical service to INACTIVE when deactivating by id.
        UUID id = UUID.randomUUID();
        MedicalService entity = buildMedicalService();
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        assertThat(medicalServiceService.deactivateMedicalService(id)).isEqualTo(1);
        assertThat(entity.getStatus()).isEqualTo(MedicalServiceStatus.INACTIVE);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("MED-SRV-UT-014 - Reject deactivate by id when medical service is missing")
    void deactivateMedicalServiceShouldThrowWhenMissing() {
        // Note: MED-SRV-UT-014 | Objective: throw exception when deactivating a medical service id that does not exist.
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalServiceService.deactivateMedicalService(id))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("MED-SRV-UT-015 - Deactivate medical service by type successfully")
    void deactivateMedicalServiceTypeShouldReturnUpdatedRows() {
        // Note: MED-SRV-UT-015 | Objective: return updated row count when deactivating medical services by type.
        when(repository.deactivateMedicalServiceType("IMAGING")).thenReturn(3);

        assertThat(medicalServiceService.deactivateMedicalServiceType("IMAGING")).isEqualTo(3);
    }

    @Test
    @DisplayName("MED-SRV-UT-016 - Reject deactivate by type when nothing is updated")
    void deactivateMedicalServiceTypeShouldThrowWhenNothingUpdated() {
        // Note: MED-SRV-UT-016 | Objective: throw exception when no medical service matches the type for deactivation.
        when(repository.deactivateMedicalServiceType("IMAGING")).thenReturn(0);

        assertThatThrownBy(() -> medicalServiceService.deactivateMedicalServiceType("IMAGING"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    private MedicalService buildMedicalService() {
        return MedicalService.builder()
                .id(UUID.randomUUID())
                .serviceName("X-Ray")
                .serviceType("IMAGING")
                .serviceTime(20)
                .status(MedicalServiceStatus.ACTIVE)
                .price(150f)
                .description("desc")
                .imgUrl("img")
                .build();
    }

    private MedicalServiceDTO buildMedicalServiceDto(MedicalService entity) {
        return MedicalServiceDTO.builder()
                .id(entity.getId())
                .serviceName(entity.getServiceName())
                .serviceType(entity.getServiceType())
                .serviceTime(entity.getServiceTime())
                .status(entity.getStatus())
                .price(entity.getPrice())
                .description(entity.getDescription())
                .imgUrl(entity.getImgUrl())
                .build();
    }

    private MedicalServiceRequestDTO buildMedicalServiceRequest() {
        return MedicalServiceRequestDTO.builder()
                .serviceName("X-Ray")
                .serviceType("IMAGING")
                .serviceTime(20)
                .status(MedicalServiceStatus.ACTIVE)
                .price(150f)
                .description("desc")
                .imgUrl("img")
                .build();
    }
}
