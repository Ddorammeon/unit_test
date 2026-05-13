package com.do_an.invoiceservice.iservice;

import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.common.model.InvoiceItemResponse;
import com.do_an.common.model.MedicineItem;
import com.do_an.invoiceservice.dto.request.CreateInvoiceRequestDTO;
import com.do_an.invoiceservice.dto.response.InvoiceResponseDTO;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IInvoiceService {
    // Existing methods
    InvoiceResponseDTO createInvoice(CreateInvoiceRequestDTO request);
    InvoiceResponseDTO updateInvoice(UUID invoiceId, CreateInvoiceRequestDTO request);
    InvoiceResponseDTO markAsPaid(UUID invoiceId);
    InvoiceResponseDTO cancelInvoice(UUID invoiceId);
    InvoiceResponseDTO getInvoiceById(UUID invoiceId);
    List<InvoiceResponseDTO> listInvoices(String status);
    List<InvoiceResponseDTO> getInvoicesByPatientId(UUID patientId, String status);
    List<InvoiceResponseDTO> getInvoicesByAppointmentId(UUID appointmentId);
    
    // New methods for refactoring
    InvoiceResponseDTO addMedicineCharges(UUID invoiceId, Set<InvoiceItemCheckerRequest> items, 
                                         List<MedicineItem> medicineItems);
    InvoiceResponseDTO applyInsuranceDiscount(UUID invoiceId, UUID insuranceClaimId, 
                                            Integer discountAmount, Set<InvoiceItemResponse> items);
    InvoiceResponseDTO revertInsuranceDiscount(UUID invoiceId);
    InvoiceResponseDTO removeMedicineCharges(UUID invoiceId);
    InvoiceResponseDTO updateStatus(UUID invoiceId, String status);
    InvoiceResponseDTO addLabTestCharge(com.do_an.invoiceservice.dto.request.AddLabTestChargeRequestDTO request);
    
    // Validation methods
    boolean canAddMedicineCharges(UUID invoiceId);
    boolean canApplyInsuranceDiscount(UUID invoiceId);
    boolean canCancel(UUID invoiceId);
    boolean canMarkAsPaid(UUID invoiceId);
}




