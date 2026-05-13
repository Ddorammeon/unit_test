package com.do_an.paymentservice.iservice;

import com.do_an.paymentservice.dto.request.CreatePaymentRequestDTO;
import com.do_an.paymentservice.dto.request.UpdatePaymentRequestDTO;
import com.do_an.paymentservice.dto.response.InvoiceResponseDTO;
import com.do_an.paymentservice.dto.response.PaymentResponseDTO;
import com.do_an.paymentservice.entity.Payment;
import com.do_an.paymentservice.entity.PaymentMethod;
import com.do_an.paymentservice.entity.PaymentStatus;

import java.util.List;
import java.util.UUID;

public interface IPaymentService {
    // Business logic methods
    PaymentResponseDTO initiatePayment(CreatePaymentRequestDTO request);
    PaymentResponseDTO handleCashPayment(CreatePaymentRequestDTO request, InvoiceResponseDTO invoice, UUID dispenseOrderId);
    PaymentResponseDTO handleBankTransferPayment(CreatePaymentRequestDTO request, InvoiceResponseDTO invoice, UUID dispenserOrderId);
    void handlePayOSWebhook(String transactionId, boolean isSuccess, String bankTransactionId);
    PaymentResponseDTO getPaymentStatusOfInvoice(UUID invoiceId);
    PaymentResponseDTO getPaymentById(UUID paymentId);
    PaymentResponseDTO updatePayment(UUID paymentId, UpdatePaymentRequestDTO request);
    void deletePayment(UUID paymentId);
    List<PaymentResponseDTO> getAllPayments(UUID invoiceId, PaymentStatus status, PaymentMethod paymentMethod);
    List<PaymentResponseDTO> getPaymentsByInvoiceId(UUID invoiceId);
    InvoiceResponseDTO validateInvoice(UUID invoiceId);
    PaymentResponseDTO handlePaymentCallback(String orderCode, String status, String code, Boolean cancel);

    // Methods for handlers
    boolean canUpdatePaymentStatus(UUID paymentId);
    void updatePaymentStatusFromEvent(UUID paymentId, PaymentStatus status, String reason);
}

