            package com.do_an.paymentservice.scheduler;//package com.do_an.paymentservice.scheduler;
//
//import com.do_an.paymentservice.entity.Payment;
//import com.do_an.paymentservice.entity.PaymentStatus;
//import com.do_an.paymentservice.repository.PaymentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class PaymentExpirationScheduler {
//
//    private final PaymentRepository paymentRepository;
//
//    /**
//     * Chạy mỗi phút để kiểm tra và cập nhật các payment đã hết hạn
//     * Chỉ cập nhật các payment có status PENDING và expiredAt đã qua
//     */
//    @Scheduled(fixedRate = 60000) // Chạy mỗi 60 giây (1 phút)
//    @Transactional
//    public void checkAndUpdateExpiredPayments() {
//        log.debug("Đang kiểm tra các payment đã hết hạn...");
//
//        LocalDateTime now = LocalDateTime.now();
//
//
//        // Sử dụng query method để tối ưu performance
//        List<Payment> expiredPayments = paymentRepository
//                .findAllByStatusAndExpiredAtBefore(PaymentStatus.PENDING, now);
//
//        if (expiredPayments.isEmpty()) {
//            log.debug("Không có payment nào hết hạn");
//            return;
//        }
//
//        log.info("Tìm thấy {} payment đã hết hạn, đang cập nhật trạng thái...", expiredPayments.size());
//
//        // Cập nhật trạng thái thành FAILED
//        for (Payment payment : expiredPayments) {
//            payment.setStatus(PaymentStatus.FAILED);
//            payment.setDescription("Thanh toán đã hết hạn - " + payment.getDescription());
//            paymentRepository.save(payment);
//            log.info("Đã cập nhật payment {} (Invoice: {}) thành FAILED do hết hạn",
//                    payment.getId(), payment.getInvoiceId());
//        }
//
//        log.info("Đã cập nhật {} payment hết hạn thành công", expiredPayments.size());
//    }
//}