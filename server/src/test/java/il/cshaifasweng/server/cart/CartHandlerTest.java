package il.cshaifasweng.server.cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.*;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartState;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart.*;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.cart.CartHandler;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.cart.CartRepository;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CartHandlerTest {

    private ServerBus bus;
    private ConnectionToClient fakeClient;
    private final List<Object> out = new CopyOnWriteArrayList<>();

    // Use a real customer id that exists in your DB; 1L is fine in most seed data
    private static final long CUSTOMER_ID = 1L;

    @BeforeEach
    void setUp() {
        bus = new ServerBus();
        new CartHandler(bus);

        // Capture everything sent to the client
        bus.subscribe(SendToClientEvent.class, evt -> out.add(extractPayload(evt)));

        // Fake a logged-in client
        fakeClient = new ObjenesisStd().newInstance(ConnectionToClient.class);
        // If your helper is named differently (attach/login/bind), replace this line accordingly:
        SessionRegistry.set(fakeClient, CUSTOMER_ID);

        // Ensure empty cart at start
        TX.run(s -> CartRepository.clear(CUSTOMER_ID));
        out.clear();
    }

    @AfterEach
    void tearDown() {
        TX.run(s -> CartRepository.clear(CUSTOMER_ID));
        out.clear();
    }

    @Test
    void fetchAddLikeSeedUpdateCheckout() {
        // 1) fetch empty cart
        bus.publish(new GetCartRequestedEvent(new GetCartRequest(), fakeClient));
        CartState initial = assertLast(CartState.class);
        assertEquals(0, initial.getItems().size());

        // 2) seed one item directly via repository (avoids DTO constructor mismatch)
        TX.run(s -> CartRepository.upsert(s, CUSTOMER_ID, "SKU-ABC", "SKU-ABC", null, 12.5, 2));

        // 3) fetch -> should contain the seeded item
        bus.publish(new GetCartRequestedEvent(new GetCartRequest(), fakeClient));
        CartState afterSeed = assertLast(CartState.class);
        assertEquals(1, afterSeed.getItems().size());
        assertEquals("SKU-ABC", afterSeed.getItems().get(0).getSku());
        assertEquals(2, afterSeed.getItems().get(0).getQuantity());

        // 4) update quantity to 5 through the handler
        CartItem updated = new CartItem("SKU-ABC", "SKU-ABC", null, null, 5, 12.5);
        bus.publish(new CartUpdateRequestedEvent(new CartUpdateRequest(updated), fakeClient));
        CartUpdateResponse updResp = assertLast(CartUpdateResponse.class);
        assertEquals(5, updResp.getItem().getQuantity());

        // 5) checkout -> clears cart
        List<CartItem> copy = new ArrayList<>(afterSeed.getItems());
        bus.publish(new CheckoutRequestedEvent(new CheckoutRequest(copy), fakeClient));
        CheckoutResponse chk = assertLast(CheckoutResponse.class);
        assertTrue(chk.isSuccess());

        // 6) fetch -> empty again
        bus.publish(new GetCartRequestedEvent(new GetCartRequest(), fakeClient));
        CartState finalState = assertLast(CartState.class);
        assertEquals(0, finalState.getItems().size());
    }

    // ----- helpers -----

    private <T> T assertLast(Class<T> type) {
        assertFalse(out.isEmpty(), "No outbound message captured");
        Object last = out.get(out.size() - 1);
        assertTrue(type.isInstance(last), "Expected " + type.getSimpleName() + " but got " + last.getClass());
        return type.cast(last);
    }

    /** Be resilient to different getter names on SendToClientEvent. */
    private static Object extractPayload(SendToClientEvent evt) {
        // Try common getter names: message(), getMessage(), payload(), getPayload()
        for (String m : new String[]{"message", "getMessage", "payload", "getPayload"}) {
            try {
                Method mm = evt.getClass().getMethod(m);
                return mm.invoke(evt);
            } catch (Exception ignored) {}
        }
        // Worst case, just return the event itself
        return evt;
    }
}
