package il.cshaifasweng.OCSFMediatorExample.server.handlers.reports;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Stores;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresError;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.StoreOption;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Reports.GetStoresRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.util.List;
import java.util.stream.Collectors;

public class GetStoresHandler {
    public GetStoresHandler(ServerBus bus) {
        bus.subscribe(GetStoresRequestedEvent.class, evt -> {
            try {
                List<Stores> stores = TX.call(s ->
                        s.createQuery("from Stores", Stores.class).list()
                );

                List<StoreOption> options = stores.stream()
                        .map(st -> new StoreOption(String.valueOf(st.getId()), st.getName()))
                        .collect(Collectors.toList());

                bus.publish(new SendToClientEvent(new GetStoresResponse(options), evt.getClient()));
            } catch (Exception e) {
                bus.publish(new SendToClientEvent(new GetStoresError("Failed loading stores: " + e.getMessage()), evt.getClient()));
            }
        });
    }
}
