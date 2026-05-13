package com.do_an.invoiceservice.mapper;

import com.do_an.invoiceservice.entity.InvoiceItem;
import com.do_an.common.model.InvoiceItemCheckerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceItemSagaMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "referenceId", source = "referenceId")
    @Mapping(target = "serviceType", source = "serviceType")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "unitPrice", source = "unitPrice")
    InvoiceItemCheckerRequest toDto(InvoiceItem entity);
}
