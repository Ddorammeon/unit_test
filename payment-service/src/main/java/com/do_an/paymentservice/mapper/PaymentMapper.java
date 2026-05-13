package com.do_an.paymentservice.mapper;

import com.do_an.paymentservice.dto.response.PaymentResponseDTO;
import com.do_an.paymentservice.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(source = "id", target = "paymentId")
    @Mapping(source = "description", target = "message")
    PaymentResponseDTO toResponseDto(Payment payment);
}
