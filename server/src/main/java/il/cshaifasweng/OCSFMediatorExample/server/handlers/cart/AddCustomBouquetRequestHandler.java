package il.cshaifasweng.OCSFMediatorExample.server.handlers.cart;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.CartItemRow;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.AddCustomBouquetRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.AddCustomBouquetResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart.AddCustomBouquetRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.Session;

import java.time.LocalDateTime;

public class AddCustomBouquetRequestHandler {

    private final ServerBus bus;

    public AddCustomBouquetRequestHandler(ServerBus bus) {
        this.bus = bus;
        bus.subscribe(AddCustomBouquetRequestEvent.class, this::onAddCustomBouquet);
    }

    private void onAddCustomBouquet(AddCustomBouquetRequestEvent event) {
        ConnectionToClient client = event.client();
        AddCustomBouquetRequest req = event.req();

        try {
            Long customerId = SessionRegistry.get(client);
            if (customerId == null) {
                bus.publish(new SendToClientEvent(
                        new AddCustomBouquetResponse(false, "Not logged in"), client));
                return;
            }

            TX.run((Session s) -> {
                // === 1. Create and persist cart item ===
                CartItemRow item = new CartItemRow();
                item.setCustomerId(customerId);
                item.setSku("CUSTOM-" + System.currentTimeMillis());
                item.setName("Custom Bouquet");
                item.setPictureUrl("images/custom_bouquet_placeholder.jpg");
                item.setUnitPrice(req.getTotalPrice());
                item.setQuantity(1);
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());

                s.persist(item);
            });

            // === 2. Success response ===
            AddCustomBouquetResponse res = new AddCustomBouquetResponse(
                    true,
                    "Custom bouquet added to cart successfully."
            );
            bus.publish(new SendToClientEvent(res, client));

        } catch (Exception e) {
            e.printStackTrace();
            AddCustomBouquetResponse res = new AddCustomBouquetResponse(
                    false,
                    "Failed to add custom bouquet: " + e.getMessage()
            );
            bus.publish(new SendToClientEvent(res, client));
        }
    }
}
