package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.dto.CustomerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.SetPremiumRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.SetPremiumResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.SetPremiumRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.Session;

import java.time.Instant;

public class PremiumHandler {

    private final ServerBus bus;

    public PremiumHandler(ServerBus bus) {
        this.bus = bus;
        bus.subscribe(SetPremiumRequestedEvent.class, this::onSetPremium);
    }

    private void onSetPremium(SetPremiumRequestedEvent evt) {
        ConnectionToClient client = evt.getClient();
        SetPremiumRequest req = evt.getRequest();

        Long cid = SessionRegistry.get(client);
        if (cid == null) {
            send(client, SetPremiumResponse.fail("Not logged in"));
            return;
        }

        try {
            TX.run((Session s) -> {
                Customer c = s.get(Customer.class, cid);
                if (c == null) throw new IllegalStateException("Customer not found: " + cid);

                boolean makePremium = req.isPremium();
                c.setPremium(makePremium);
                c.setPremiumSince(makePremium ? Instant.now() : null);

                s.merge(c);
            });

            send(client, SetPremiumResponse.ok());

            // Push fresh overview snapshot
            TX.run((Session s) -> {
                Customer c = s.get(Customer.class, cid);
                send(client, AccountOverviewResponse.success(CustomerDTO.from(c)));
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            send(client, SetPremiumResponse.fail("Server error"));
        }
    }

    private void send(ConnectionToClient client, Object payload) {
        bus.publish(new SendToClientEvent(payload, client));
    }
}
