package il.cshaifasweng.OCSFMediatorExample.server.handlers.Flower;

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
            String sku = evt.request().getSku();   // The unique flower SKU
            System.out.printf("[FLOWER] Delete sku=%s%n", sku);

            try {
                boolean deleted = TXDeleteFlower(sku);

                if (deleted) {
                    bus.publish(new SendToClientEvent(
                            new DeleteFlowerResponse(true, null),
                            evt.client()
                    ));
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
                s.remove(f);     // Hibernate DELETE
                removed[0] = true;
            }
        });
        return removed[0];
    }
}
