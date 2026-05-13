package com.do_an.invoiceservice.mapper;

import com.do_an.common.model.InvoiceItemResponse;
import com.do_an.invoiceservice.dto.request.CreateInvoiceItemRequestDTO;
import com.do_an.invoiceservice.dto.response.InvoiceItemResponseDTO;
import com.do_an.invoiceservice.entity.InvoiceItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface InvoiceItemMapper {

    // --- Chiều 1: Entity -> ResponseDTO ---
    // (Phần này của bạn đã đúng)
    @Mappings({
            @Mapping(target = "itemTotal", expression = "java(item.getQuantity() * item.getUnitPrice())")
    })
    InvoiceItemResponseDTO toResponseDto(InvoiceItem item);

    // --- Chiều 2: CreateDTO -> Entity ---
    // (ĐÂY LÀ PHẦN SỬA LỖI)
    @Mappings({
            @Mapping(target = "invoice", ignore = true),
            @Mapping(target = "insurancePayAmount", ignore = true),
            @Mapping(target = "patientPayAmount", ignore = true)
    })
    // SỬA LỖI: Tham số phải là "CreateInvoiceItemDTO", không phải "InvoiceItemResponseDTO"
    InvoiceItem toEntity(CreateInvoiceItemRequestDTO dto);


    // --- BỔ SUNG: Hàm cập nhật Entity đã tồn tại ---
    // (Cần thiết cho logic "Update Invoice" của bạn)
    @Mappings({
            @Mapping(target = "id", ignore = true), // Không bao giờ map ID
            @Mapping(target = "invoice", ignore = true),
            @Mapping(target = "insurancePayAmount", ignore = true),
            @Mapping(target = "patientPayAmount", ignore = true)
    })
    void updateFromDto(CreateInvoiceItemRequestDTO dto, @MappingTarget InvoiceItem entity);


    @Mappings({
            @Mapping(target = "invoice", ignore = true),
            @Mapping(target = "id", ignore = true)
    })
    void updateFromResponse(InvoiceItemResponse response, @MappingTarget InvoiceItem entity);

}
