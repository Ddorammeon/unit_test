package com.do_an.prescriptionbillingservice.util;

import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.prescriptionbillingservice.dto.response.InvoiceItemResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceItemMapper {

    InvoiceItemCheckerRequest toCheckerRequest(InvoiceItemResponseDTO dto);

    List<InvoiceItemCheckerRequest> toCheckerRequests(List<InvoiceItemResponseDTO> dtoList);
}

