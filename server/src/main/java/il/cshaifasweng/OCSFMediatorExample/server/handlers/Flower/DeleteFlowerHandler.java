package il.cshaifasweng.OCSFMediatorExample.server.handlers.Flower;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.FlowerUpdatedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToAllClientsEvent;
import org.hibernate.Session;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.DeleteFlowerResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Flowers.DeleteFlowerRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import jakarta.validation.ConstraintViolationException;

public class DeleteFlowerHandler {
    public DeleteFlowerHandler(ServerBus bus) {
        bus.subscribe(DeleteFlowerRequestEvent.class, evt -> {
            String sku = evt.request().getSku();
            System.out.printf("[FLOWER] Delete sku=%s%n", sku);

            try {
                boolean deleted = TXDeleteFlower(sku);

                if (deleted) {
                    // === Send response back to requesting client ===
                    bus.publish(new SendToClientEvent(
                            new DeleteFlowerResponse(true, null),
                            evt.client()
                    ));

                    // === Broadcast update to all connected clients ===
                    bus.publish(new SendToAllClientsEvent(
                            new FlowerUpdatedEvent(null)  // no DTO, just signal to refresh
                    ));

                    System.out.println("[FLOWER] Broadcasted FlowerUpdatedEvent (delete) to all clients");
                } else {
                    bus.publish(new SendToClientEvent(
                            new DeleteFlowerResponse(false, "Flower not found"),
                            evt.client()
                    ));
                }

            } catch (ConstraintViolationException fk) {
                bus.publish(new SendToClientEvent(
                        new DeleteFlowerResponse(false,
                                "Cannot delete: this flower is referenced by another record (e.g., orders or promotions)."),
                        evt.client()
                ));
            } catch (Exception ex) {
                ex.printStackTrace();
                bus.publish(new SendToClientEvent(
                        new DeleteFlowerResponse(false, "Delete failed: " + ex.getMessage()),
                        evt.client()
                ));
            }
        });
    }

    /** Transactionally removes a flower by SKU. Returns true if it was removed. */
    private boolean TXDeleteFlower(String sku) {
        final boolean[] removed = { false };
        TX.run((Session s) -> {
            Flower f = s.get(Flower.class, sku);
            if (f != null) {
                s.remove(f);
                removed[0] = true;
            }
        });
        return removed[0];
    }
}
