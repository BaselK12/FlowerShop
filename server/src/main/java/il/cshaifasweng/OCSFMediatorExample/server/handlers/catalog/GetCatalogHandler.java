package il.cshaifasweng.OCSFMediatorExample.server.handlers.catalog;


import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Catalog.GetCatalogRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.FlowerMapper;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.util.List;


public class GetCatalogHandler {
    public GetCatalogHandler(ServerBus bus) {
        bus.subscribe(GetCatalogRequestEvent.class, evt -> {
            GetCatalogRequest req = evt.request();
            try {
                List<Flower> results = TX.call(s -> {
                    StringBuilder jpql = new StringBuilder(
                            "SELECT DISTINCT f FROM Flower f " +
                                    "LEFT JOIN FETCH f.categories " +  // fixed here
                                    "LEFT JOIN FETCH f.promotion " +
                                    "WHERE 1=1"
                    );

                    if (req.getCategory() != null && !req.getCategory().isBlank()) {
                        // also fixed here
                        jpql.append(" AND EXISTS (SELECT c FROM f.categories c WHERE c.name = :cat)");
                    }
                    if (req.getPromotionId() != null) {
                        jpql.append(" AND f.promotion.id = :promoId");
                    }
                    if (req.getSearchText() != null && !req.getSearchText().isBlank()) {
                        jpql.append(" AND (LOWER(f.name) LIKE :q OR LOWER(f.description) LIKE :q)");
                    }
                    if (req.isOnlyActivePromotions()) {
                        jpql.append(" AND f.promotion.active = true");
                    }

                    var query = s.createQuery(jpql.toString(), Flower.class);

                    if (req.getCategory() != null && !req.getCategory().isBlank())
                        query.setParameter("cat", req.getCategory());
                    if (req.getPromotionId() != null)
                        query.setParameter("promoId", req.getPromotionId());
                    if (req.getSearchText() != null && !req.getSearchText().isBlank())
                        query.setParameter("q", "%" + req.getSearchText().toLowerCase() + "%");

                    return query.getResultList();
                });

                List<FlowerDTO> payload = results.stream()
                        .map(FlowerMapper::toDTO)
                        .toList();

                bus.publish(new SendToClientEvent(new GetCatalogResponse(payload), evt.client()));
            } catch (Exception ex) {
                ex.printStackTrace();
                bus.publish(new SendToClientEvent(new GetCatalogResponse(List.of()), evt.client()));
            }
        });
    }
}
