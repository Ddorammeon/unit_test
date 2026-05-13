package com.do_an.paymentservice.controller;

import com.do_an.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final PayOS payOS;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Webhook từ payOS v1.0.3
     * POST /api/payments/webhook/payos
     *
     * Webhook Body từ payOS:
     * {
     *   "code": "00",
     *   "desc": "success",
     *   "success": true,
     *   "data": {
     *     "orderCode": 123456789,
     *     "amount": 1000000,
     *     "description": "Thanh toán hóa đơn",
     *     "accountNumber": "0399609015",
     *     "reference": "TF230204212323",
     *     "transactionDateTime": "2023-02-04 18:25:00",
     *     "currency": "VND",
     *     "paymentLinkId": "124c33293c43417ab7879e14c8d9eb18",
     *     ...
     *   },
     *   "signature": "8d8640d802576397a1ce45ebda7f835055768ac7ad2e0bfb77f9b8f12cca4c7f"
     * }
     */
    @PostMapping("/payos")
        public ResponseEntity<?> handlePayOSWebhook(@RequestBody String jsonBody) {
        log.info("===== Nhận Webhook từ payOS v1.0.3 =====");
        log.debug("Webhook Body: {}", jsonBody);

        try {
            // 1. Parse JSON body thành Webhook object (v1.0.3 requirement)
            Webhook webhookBody = objectMapper.readValue(jsonBody, Webhook.class);
            log.debug("✅ Parsed webhook body thành Webhook object");

            // 2. Xác thực webhook từ payOS v1.0.3 (dùng Webhook object, không phải String)
            // ✅ ĐÚNG: verifyPaymentWebhookData(Webhook) - nhận tham số Webhook
            WebhookData webhookData = payOS.verifyPaymentWebhookData(webhookBody);

            log.info("✅ Webhook xác thực thành công");
            log.info("Success: {}", webhookData.getDesc());

            // 3. Lấy thông tin từ webhook
            // ✅ ĐÚNG: getOrderCode() trả về long
            long orderCode = webhookData.getOrderCode();
            String transactionId = String.valueOf(orderCode);

            // ✅ ĐÚNG: Các trường khác được map từ WebhookData
            String code = webhookData.getCode();           // "00" = success
            String desc = webhookData.getDesc();            // "Thành công"
            int amount = webhookData.getAmount();           // số tiền (int trong v1.0.3)
            String description = webhookData.getDescription(); // mô tả giao dịch
            String accountNumber = webhookData.getAccountNumber();
            String reference = webhookData.getReference();   // Reference ID từ ngân hàng
            String transactionDateTime = webhookData.getTransactionDateTime();
            String currency = webhookData.getCurrency();
            String paymentLinkId = webhookData.getPaymentLinkId();

            // Các trường ngân hàng đối ứng (nếu có)
            String counterAccountBankId = webhookData.getCounterAccountBankId();
            String counterAccountBankName = webhookData.getCounterAccountBankName();
            String counterAccountName = webhookData.getCounterAccountName();
            String counterAccountNumber = webhookData.getCounterAccountNumber();

            // Các trường tài khoản ảo (nếu có)
            String virtualAccountName = webhookData.getVirtualAccountName();
            String virtualAccountNumber = webhookData.getVirtualAccountNumber();

            log.info("━━━━━━━ Webhook Details (v1.0.3) ━━━━━━━");
            log.info("OrderCode: {}", orderCode);
            log.info("TransactionId: {}", transactionId);
            log.info("Code: {}", code);
            log.info("Description: {}", desc);
            log.info("Amount: {}", amount);
            log.info("Description (detail): {}", description);
            log.info("AccountNumber: {}", accountNumber);
            log.info("Reference (BankTxId): {}", reference);
            log.info("TransactionDateTime: {}", transactionDateTime);
            log.info("Currency: {}", currency);
            log.info("PaymentLinkId: {}", paymentLinkId);
            log.info("CounterAccountBankId: {}", counterAccountBankId);
            log.info("CounterAccountBankName: {}", counterAccountBankName);
            log.info("CounterAccountName: {}", counterAccountName);
            log.info("CounterAccountNumber: {}", counterAccountNumber);
            log.info("VirtualAccountName: {}", virtualAccountName);
            log.info("VirtualAccountNumber: {}", virtualAccountNumber);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // 4. Xác định thanh toán thành công
            // ✅ ĐÚNG: kiểm tra success flag và code "00"
            boolean isSuccess = "00".equals(code) ;


            log.info("Thanh toán thành công? {}", isSuccess);

            // 5. Gửi tới Service xử lý
            // reference từ ngân hàng được lưu làm bankTransactionId

            paymentService.handlePayOSWebhook(transactionId, isSuccess, reference);

            // 6. Trả về response thành công cho payOS (bắt buộc!)
            log.info("✅ Trả response thành công cho payOS");
            return ResponseEntity.ok("{\"error\":0,\"message\":\"Success\"}");

        } catch (IllegalArgumentException e) {
            // Lỗi xác thực signature (webhook fake hoặc signature không hợp lệ)
            log.error("❌ Lỗi xác thực Webhook signature: {}", e.getMessage());
            log.error("Chi tiết lỗi: ", e);
            return ResponseEntity.badRequest()
                    .body("{\"error\":-1,\"message\":\"Webhook signature verification failed\"}");

        } catch (Exception e) {
            log.error("❌ Lỗi xử lý Webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\":-1,\"message\":\"Internal error: " + e.getMessage() + "\"}");
        }
    }
}
