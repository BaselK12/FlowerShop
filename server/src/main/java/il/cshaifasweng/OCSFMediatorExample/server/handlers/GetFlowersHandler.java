package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.GetFlowersRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.GetFlowersResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.GetFlowersRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.FlowerMapper;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.util.List;

public class GetFlowersHandler {
    public GetFlowersHandler(ServerBus bus) {
        bus.subscribe(GetFlowersRequestEvent.class, evt -> {
            GetFlowersRequest req = evt.request();
            try {
                List<Flower> results = TX.call(s -> {
                    // Only fetch single-stem flowers
                    String hql = "SELECT f FROM Flower f " +
                            "LEFT JOIN FETCH f.categories " +
                            "LEFT JOIN FETCH f.promotion " +
                            "WHERE f.isSingle = true";
                    return s.createQuery(hql, Flower.class).getResultList();
                });

                List<FlowerDTO> payload = results.stream()
                        .map(FlowerMapper::toDTO)
                        .toList();

                bus.publish(new SendToClientEvent(
                        new GetFlowersResponse(payload, "Loaded " + payload.size() + " single flowers."),
                        evt.client()
                ));
            } catch (Exception ex) {
                ex.printStackTrace();
                bus.publish(new SendToClientEvent(
                        new GetFlowersResponse(List.of(), "Error loading flowers."),
                        evt.client()
                ));
            }
        });
    }
}
