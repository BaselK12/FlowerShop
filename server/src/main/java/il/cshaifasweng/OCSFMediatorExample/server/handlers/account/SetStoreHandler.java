package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Stores;
import il.cshaifasweng.OCSFMediatorExample.entities.dto.CustomerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.SetStoreRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.SetStoreResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.SetStoreRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.Session;

public class SetStoreHandler {

    private final ServerBus bus;

    public SetStoreHandler(ServerBus bus) {
        this.bus = bus;
        bus.subscribe(SetStoreRequestedEvent.class, this::on);
    }

    private void on(SetStoreRequestedEvent evt) {
        ConnectionToClient client = evt.getClient();
        SetStoreRequest req = evt.getRequest();

        try {
            Long cid = SessionRegistry.get(client);
            if (cid == null) {
                send(client, SetStoreResponse.fail("Not logged in"));
                return;
            }

            TX.run((Session s) -> {
                Customer c = s.get(Customer.class, cid);
                if (c == null) {
                    send(client, SetStoreResponse.fail("Customer not found"));
                    return;
                }

                Long storeId = req.getStoreId();
                if (storeId == null) {
                    c.setStore(null); // Global
                } else {
                    Stores st = s.get(Stores.class, storeId);
                    if (st == null) {
                        send(client, SetStoreResponse.fail("Store not found"));
                        return;
                    }
                    c.setStore(st);
                }

                s.merge(c);
                send(client, SetStoreResponse.ok());
                // also push fresh overview so UI updates immediately
                send(client, AccountOverviewResponse.success(CustomerDTO.from(c)));
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            send(client, SetStoreResponse.fail("Server error"));
        }
    }

    private void send(ConnectionToClient client, Object payload) {
        bus.publish(new SendToClientEvent(payload, client));
    }
}
