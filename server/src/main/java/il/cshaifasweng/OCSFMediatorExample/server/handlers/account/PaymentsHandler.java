package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Payment;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.*;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.AddPaymentRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.GetPaymentsRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.RemovePaymentRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PaymentsHandler {

    public PaymentsHandler(ServerBus bus) {
        bus.subscribe(GetPaymentsRequestedEvent.class, evt -> {
            Long cid = SessionRegistry.get(evt.getClient());
            List<Payment> list = TX.call(session ->
                    session.createQuery(
                                    "from Payment p where p.customerId = :cid order by p.createdAt desc",
                                    Payment.class
                            ).setParameter("cid", cid == null ? -1L : cid)
                            .getResultList()
            );
            bus.publish(new SendToClientEvent(new GetPaymentsResponse(list), evt.getClient()));
        });

        bus.subscribe(AddPaymentRequestedEvent.class, evt -> {
            Long cid = SessionRegistry.get(evt.getClient());
            Payment saved = TX.call(session -> {
                PaymentDTO dto = evt.getRequest().getPayment();

                Payment p = new Payment();
                p.setId(generatePaymentId());                  // String PK like "PAY_abc123"
                p.setCustomerId(cid);
                p.setMethod(dto.getMethod() == null ? Payment.Method.CREDIT_CARD : dto.getMethod());
                p.setMaskedCardNumber(dto.getMaskedCardNumber());
                p.setCardHolderName(dto.getCardHolderName());
                p.setExpirationDate(dto.getExpirationDate());
                p.setAmount(dto.getAmount() == null ? 0.0 : dto.getAmount());
                p.setStatus(Payment.Status.AUTHORIZED);
                p.setCreatedAt(LocalDateTime.now());

                session.persist(p);
                return p;
            });

            bus.publish(new SendToClientEvent(new AddPaymentResponse(saved), evt.getClient()));
        });

        bus.subscribe(RemovePaymentRequestedEvent.class, evt -> {
            Long cid = SessionRegistry.get(evt.getClient());
            String id = evt.getRequest().getPaymentId();

            int deleted = TX.call(session ->
                    session.createMutationQuery(
                                    "delete from Payment p where p.id = :id and p.customerId = :cid"
                            ).setParameter("id", id)
                            .setParameter("cid", cid == null ? -1L : cid)
                            .executeUpdate()
            );

            bus.publish(new SendToClientEvent(new RemovePaymentResponse(deleted > 0, id), evt.getClient()));
        });
    }

    private static String generatePaymentId() {
        // Short, unique, String PK. Good enough for dev.
        return "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        // If you prefer numeric ids, refactor entity + messages to use Long and AUTO_INCREMENT.
    }
}
