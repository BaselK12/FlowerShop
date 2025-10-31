package il.cshaifasweng.OCSFMediatorExample.server.handlers.Flower;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Category;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.SaveFlowerRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.SaveFlowerResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.FlowerUpdatedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Flowers.SaveFlowerRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToAllClientsEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.FlowerMapper;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.Hibernate;

import java.util.List;

public class SaveFlowerHandler {

    public SaveFlowerHandler(ServerBus bus) {
        bus.subscribe(SaveFlowerRequestEvent.class, evt -> {
            SaveFlowerRequest req = evt.request();
            SaveFlowerRequest.ActionType action = req.getAction();

            try {
                Flower saved = TX.call(s -> {
                    Flower f;

                    // ===== Load or create entity =====
                    if (action == SaveFlowerRequest.ActionType.EDIT) {
                        f = s.get(Flower.class, req.getSku());
                        if (f == null) {
                            throw new IllegalArgumentException(
                                    "Flower with SKU '" + req.getSku() + "' not found.");
                        }
                    } else {
                        f = new Flower();
                        f.setSku(req.getSku());
                    }

                    // ===== Update common fields =====
                    f.setName(req.getName());
                    f.setDescription(req.getDescription());
                    f.setPrice(req.getPrice());
                    f.setImageUrl(req.getImageUrl());

                    // ===== Update categories =====
                    List<String> categoryNames = req.getCategories();
                    if (categoryNames != null && !categoryNames.isEmpty()) {
                        List<Category> matched = s.createQuery(
                                        "FROM Category c WHERE lower(c.name) IN :names OR lower(c.displayName) IN :names",
                                        Category.class)
                                .setParameter("names", categoryNames.stream()
                                        .map(String::toLowerCase)
                                        .toList())
                                .getResultList();
                        f.setCategory(matched);
                    } else {
                        f.setCategory(null);
                    }

                    // ===== Persist or merge =====
                    if (action == SaveFlowerRequest.ActionType.ADD) {
                        s.persist(f);
                    } else {
                        s.merge(f);
                    }

                    // ===== Initialize lazy fields before returning =====
                    Hibernate.initialize(f.getPromotion());
                    Hibernate.initialize(f.getCategory());

                    return f;
                });

                // ===== Map to DTO safely after transaction =====
                FlowerDTO dto = FlowerMapper.toDTO(saved);

                // ===== Send structured response to client =====
                SaveFlowerResponse response = new SaveFlowerResponse(true, null, dto);
                bus.publish(new SendToClientEvent(response, evt.client()));

                // ===== Broadcast update to all connected clients =====
                bus.publish(new SendToAllClientsEvent(new FlowerUpdatedEvent(dto)));

                // (Optional) Broadcast update to all clients
                // bus.publish(new SendToAllClientsEvent(new FlowerUpdatedEvent(dto)));

            } catch (Exception ex) {
                ex.printStackTrace();
                SaveFlowerResponse response = new SaveFlowerResponse(
                        false,
                        "Failed to " + (req.getAction() == SaveFlowerRequest.ActionType.ADD ? "add" : "edit") +
                                " flower: " + ex.getMessage(),
                        null
                );
                bus.publish(new SendToClientEvent(response, evt.client()));
            }
        });
    }
}
