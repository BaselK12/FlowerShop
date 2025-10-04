package il.cshaifasweng.OCSFMediatorExample.server.handlers.catalog;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Promotion;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetPromotionsResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.PromotionDTO;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Catalog.GetPromotionsRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.time.LocalDate;
import java.util.List;

public class GetPromotionsHandler {
    public GetPromotionsHandler(ServerBus bus) {
        bus.subscribe(GetPromotionsRequestEvent.class, evt -> {
            try {
                LocalDate today = LocalDate.now();

                List<Promotion> results = TX.call(s ->
                        s.createQuery(
                                        "from Promotion p where p.validFrom <= :today and p.validTo >= :today",
                                        Promotion.class
                                ).setParameter("today", today)
                                .getResultList()
                );

                List<PromotionDTO> payload = results.stream()
                        .map(p -> new PromotionDTO(
                                p.getId(),
                                p.getName(),
                                p.getDescription(),
                                p.getType().name(),
                                p.getAmount(),
                                p.getValidFrom(),
                                p.getValidTo(),
                                true // computed active
                        ))
                        .toList();

                bus.publish(new SendToClientEvent(new GetPromotionsResponse(payload), evt.client()));
            } catch (Exception e) {
                e.printStackTrace();
                bus.publish(new SendToClientEvent(new GetPromotionsResponse(List.of()), evt.client()));
            }
        });
    }
}