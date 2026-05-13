package com.do_an.invoiceservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddLabTestChargeRequestDTO {
    @NotNull
    private UUID labTestId;
    @NotNull
    private UUID appointmentId;
    @NotNull
    private Integer price;
    private String description;
}