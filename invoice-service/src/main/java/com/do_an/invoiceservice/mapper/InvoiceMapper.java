package com.do_an.invoiceservice.mapper;

import com.do_an.invoiceservice.dto.request.CreateInvoiceRequestDTO;
import com.do_an.invoiceservice.dto.response.InvoiceResponseDTO;
import com.do_an.invoiceservice.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring", uses = {InvoiceItemMapper.class})
public interface InvoiceMapper {


    // --- Chiều 1: Entity -> ResponseDTO ---
    // (Phần này của bạn đã đúng)
    @Mapping(target = "items", source = "items")
    InvoiceResponseDTO toResponseDto(Invoice invoice);

    List<InvoiceResponseDTO> toResponseDtoList(List<Invoice> invoices);

    // --- Chiều 2: CreateDTO -> Entity ---
    // (Phần này của bạn đã đúng và giờ sẽ compile được)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "totalAmount", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "updateAt", ignore = true),
            @Mapping(target = "issueAt", ignore = true),
            @Mapping(target = "paidAt", ignore = true)
    })
    Invoice toEntity(CreateInvoiceRequestDTO dto); // Thay đổi type


    // --- BỔ SUNG: Hàm cập nhật Entity đã tồn tại ---
    // (Cần thiết cho logic "Update Invoice" của bạn)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "totalAmount", ignore = true), // Sẽ được tính toán lại
            @Mapping(target = "status", ignore = true), // Không cho phép DTO đổi status
            @Mapping(target = "updateAt", ignore = true),
            @Mapping(target = "issueAt", ignore = true),
            @Mapping(target = "paidAt", ignore = true),
            @Mapping(target = "items", ignore = true) // items phải được xử lý thủ công (sync)
    })
    void updateFromDto(CreateInvoiceRequestDTO dto, @MappingTarget Invoice entity); // Thay đổi type
}
