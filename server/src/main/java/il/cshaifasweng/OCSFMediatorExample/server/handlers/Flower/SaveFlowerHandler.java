package il.cshaifasweng.OCSFMediatorExample.server.handlers.Flower;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.SaveFlowerRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Flowers.SaveFlowerRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.util.List;

public class SaveFlowerHandler {
    public SaveFlowerHandler(ServerBus bus) {
        bus.subscribe(SaveFlowerRequestEvent.class, evt -> {
            SaveFlowerRequest req = evt.request();
            SaveFlowerRequest.ActionType action = req.getAction();

            try {
                Flower saved = TX.call(s -> {
                    Flower f;

                    if (action == SaveFlowerRequest.ActionType.EDIT) {
                        f = s.get(Flower.class, req.getSku());
                        if (f == null) {
                            throw new IllegalArgumentException("Flower with SKU '" + req.getSku() + "' not found.");
                        }
                    } else {
                        f = new Flower();
                        f.setSku(req.getSku());
                    }

                    // Common field updates
                    f.setName(req.getName());
                    f.setDescription(req.getDescription());
                    f.setPrice(req.getPrice());
                    f.setImageUrl(req.getImageUrl());

                    // Optional: handle category names (if you later add Category entity)
                    List<String> categories = req.getCategories();
                    if (categories != null && !categories.isEmpty()) {
                        f.setShortDescription(String.join(", ", categories));
                    }

                    // Persist or merge
                    if (action == SaveFlowerRequest.ActionType.ADD) {
                        s.persist(f);
                    } else {
                        s.merge(f);
                    }

                    return f;
                });

                // Build success message
                String message = (action == SaveFlowerRequest.ActionType.ADD)
                        ? "Flower added successfully: " + saved.getName()
                        : "Flower updated successfully: " + saved.getName();

                // Publish back to the requesting client
                bus.publish(new SendToClientEvent(message, evt.client()));

            } catch (Exception ex) {
                ex.printStackTrace();
                String errorMsg = "Failed to " + (req.getAction() == SaveFlowerRequest.ActionType.ADD ? "add" : "edit")
                        + " flower: " + ex.getMessage();
                bus.publish(new SendToClientEvent(errorMsg, evt.client()));
            }
        });
    }
}
