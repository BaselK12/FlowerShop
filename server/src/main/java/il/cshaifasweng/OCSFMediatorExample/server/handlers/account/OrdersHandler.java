package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.GetOrdersRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.GetOrdersResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.GetOrdersRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;

import java.util.List;

public class OrdersHandler {
    public OrdersHandler(ServerBus bus) {
        bus.subscribe(GetOrdersRequestedEvent.class, evt -> {
            try {
                Long sessionCustomerId = SessionRegistry.get(evt.getClient());
                if (sessionCustomerId == null) {
                    bus.publish(new SendToClientEvent(new GetOrdersResponse(List.of()), evt.getClient()));
                    return;
                }

                // Ignore the request.customerId and trust the session.
                var orders = OrdersRepository.findByCustomer(sessionCustomerId);
                bus.publish(new SendToClientEvent(new GetOrdersResponse(orders), evt.getClient()));
            } catch (Exception e) {
                e.printStackTrace();
                bus.publish(new SendToClientEvent(new GetOrdersResponse(List.of()), evt.getClient()));
            }
        });
    }
}
