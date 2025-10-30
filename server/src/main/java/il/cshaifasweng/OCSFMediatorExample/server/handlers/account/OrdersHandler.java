package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.GetOrdersRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.GetOrdersResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.GetOrdersRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
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

                // Query actual database using Hibernate Session + TX.call()
                List<Order> orders = TX.call(s ->
                        s.createQuery(
                                        "FROM Order WHERE customerId = :cid ORDER BY createdAt DESC",
                                        Order.class
                                )
                                .setParameter("cid", sessionCustomerId)
                                .list()
                );

                bus.publish(new SendToClientEvent(new GetOrdersResponse(orders), evt.getClient()));

            } catch (Exception e) {
                e.printStackTrace();
                bus.publish(new SendToClientEvent(new GetOrdersResponse(List.of()), evt.getClient()));
            }
        });
    }
}
