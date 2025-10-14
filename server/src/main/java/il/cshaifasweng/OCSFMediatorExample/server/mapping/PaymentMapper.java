package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Payment;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.PaymentDTO;

import java.time.LocalDateTime;

public class PaymentMapper {

    public static Payment fromDTO(PaymentDTO dto) {
        if (dto == null) return null;

        Payment payment = new Payment();
        payment.setMethod(Payment.Method.CREDIT_CARD);
        payment.setStatus(Payment.Status.AUTHORIZED);

        payment.setMaskedCardNumber(dto.getCardNumberMasked());
        payment.setCardHolderName(dto.getCardHolderName());
        payment.setExpirationDate(dto.getExpirationDate());


        payment.setAmount(dto.getAmount());

        if (payment.getCreatedAt() == null) {
            payment.setCreatedAt(LocalDateTime.now());
        }
        return payment;
    }

    public static PaymentDTO toDTO(Payment entity) {
        if (entity == null) return null;

        PaymentDTO dto = new PaymentDTO();
        dto.setCardNumberMasked(entity.getMaskedCardNumber());
        dto.setCardHolderName(entity.getCardHolderName());
        dto.setExpirationDate(entity.getExpirationDate());


        dto.setAmount(entity.getAmount() != null ? entity.getAmount() : 0.0);

        return dto;
    }
}
