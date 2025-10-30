package il.cshaifasweng.OCSFMediatorExample.server.handlers.Admin;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Promotion;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.AddPromotionsRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.AddPromotionsResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.AddPromotionsRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.PromotionMapper;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.PromotionDTO;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class AddPromotionsRequestHandler {

    private final ServerBus bus;

    public AddPromotionsRequestHandler(ServerBus bus) {
        this.bus = bus;
        bus.subscribe(AddPromotionsRequestEvent.class, this::onAddPromotion);
    }

    private void onAddPromotion(AddPromotionsRequestEvent event) {
        ConnectionToClient client = event.client();
        AddPromotionsRequest req = event.req();

        try {
            Promotion promo = TX.call((Session s) -> {
                // === 1. Create the new promotion ===
                Promotion promotion = new Promotion(
                        req.getName(),
                        req.getDescription(),
                        Promotion.DiscountType.valueOf(req.getType().toUpperCase()),
                        req.getAmount(),
                        req.getValidFrom(),
                        req.getValidTo()
                );

                s.persist(promotion); // persist promotion first (assigns ID)

                // === 2. Link promotion to selected flowers ===
                List<FlowerDTO> flowerDTOs = req.getFlowers();
                if (flowerDTOs != null && !flowerDTOs.isEmpty()) {
                    for (FlowerDTO dto : flowerDTOs) {
                        Flower flower = s.get(Flower.class, dto.getSku());
                        if (flower != null) {
                            flower.setPromotion(promotion);
                            s.merge(flower);
                        }
                    }
                }

                return promotion;
            });

            // === 3. Use the mapper instead of DTO factory ===
            PromotionDTO dto = PromotionMapper.toDto(promo);

            AddPromotionsResponse res = new AddPromotionsResponse(
                    true,
                    "Promotion added successfully.",
                    dto
            );

            bus.publish(new SendToClientEvent(res, client));// use .post (not publish)

        } catch (Exception e) {
            e.printStackTrace();
            AddPromotionsResponse res = new AddPromotionsResponse(
                    false,
                    "Failed to add promotion: " + e.getMessage()
            );
            bus.publish(new SendToClientEvent(res, client));
        }
    }
}
