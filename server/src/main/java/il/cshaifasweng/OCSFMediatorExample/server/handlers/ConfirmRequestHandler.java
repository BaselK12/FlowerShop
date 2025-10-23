package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.ConfirmRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.ConfirmResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.OrderDTO;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.ConfirmRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.account.OrdersRepository;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.OrderMapper;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.time.LocalDateTime;

public class ConfirmRequestHandler {

    public ConfirmRequestHandler(ServerBus bus) {
        bus.subscribe(ConfirmRequestEvent.class, evt -> {
            ConfirmRequest req = evt.request();
            OrderDTO dto = req.getOrder();

            try {
                Long sessionCustomerId = SessionRegistry.get(evt.client());
                Long effectiveCustomerId = (sessionCustomerId != null)
                        ? sessionCustomerId
                        : (dto != null ? dto.getCustomerId() : null);

                Order saved = TX.call(session -> {
                    Order order = OrderMapper.fromDTO(dto);

                    if (order.getCustomerId() == null) {
                        order.setCustomerId(effectiveCustomerId);
                    }
                    if (order.getCreatedAt() == null) {
                        order.setCreatedAt(LocalDateTime.now());
                    }
                    if (order.getStatus() == null) {
                        order.setStatus(Order.Status.INITIATED); // use the inner enum
                    }

                    session.persist(order);
                    return order;
                });

                if (saved.getCustomerId() != null) {
                    OrdersRepository.add(saved);
                }

                ConfirmResponse res = new ConfirmResponse(
                        saved.getId(),
                        "Order confirmed successfully",
                        true
                );
                bus.publish(new SendToClientEvent(res, evt.client()));

                System.out.println("[SERVER] Order confirmed and saved: " + saved.getId()
                        + " (customerId=" + saved.getCustomerId() + ")");

            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("[SERVER] Failed to confirm order: " + ex.getMessage());

                ConfirmResponse res = new ConfirmResponse(
                        null,
                        "Failed to confirm order: " + ex.getMessage(),
                        false
                );
                bus.publish(new SendToClientEvent(res, evt.client()));
            }
        });
    }
}
