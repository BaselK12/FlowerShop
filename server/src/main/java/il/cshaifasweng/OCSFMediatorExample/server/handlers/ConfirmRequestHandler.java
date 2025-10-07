package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Status;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.ConfirmRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.ConfirmResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.OrderDTO;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.ConfirmRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.OrderMapper;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.time.LocalDateTime;

public class ConfirmRequestHandler {
    public ConfirmRequestHandler(ServerBus bus) {
        bus.subscribe(ConfirmRequestEvent.class, evt -> {
            ConfirmRequest req = evt.request();
            OrderDTO dto = req.getOrder();

            try {
                // Save order transactionally
                Order saved = TX.call(session -> {
                    Order order = OrderMapper.fromDTO(dto);

                    // Ensure timestamps and initial status
                    order.setCreatedAt(LocalDateTime.now());
                    if (order.getStatus() == null) {
                        order.setStatus(Status.PENDING);
                    }

                    session.persist(order);
                    return order;
                });

                // Build response
                ConfirmResponse res = new ConfirmResponse(
                        saved.getId(),
                        "Order confirmed successfully",
                        true
                );

                // Send back to client
                bus.publish(new SendToClientEvent(res, evt.client()));

                System.out.println("[SERVER] Order confirmed and saved: " + saved.getId());

            } catch (Exception ex) {
                ex.printStackTrace();

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
