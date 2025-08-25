package il.cshaifasweng.OCSFMediatorExample.client.net;

import com.google.common.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.client.bus.ClientBus;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.SendMessageEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Ping;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Pong;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;

// IMPORTANT: import the AbstractClient from YOUR project’s package.
// In your repo the OCSF classes are inside your module, under ...client/ocsf.
// If your package is different, adjust this import accordingly.
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;

public class ClientBridge extends AbstractClient {

    public ClientBridge(String host, int port) {
        super(host, port);
        // Listen for “send this” events from anywhere in the client
        ClientBus.get().register(this);
    }

    @Subscribe
    public void onSend(SendMessageEvent ev) {
        try {
            // Open if not already
            if (!isConnected()) {
                openConnection();
            }
            // Ship the serializable object to the server (Ping for now)
            sendToServer(ev.payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof il.cshaifasweng.OCSFMediatorExample.entities.messages.Pong) {
            il.cshaifasweng.OCSFMediatorExample.entities.messages.Pong p =
                    (il.cshaifasweng.OCSFMediatorExample.entities.messages.Pong) msg;
            System.out.println("SERVER SAID: " + p.getReply());
            return;
        }
        if (msg instanceof LoginResponse) {
            LoginResponse r = (LoginResponse) msg;
            System.out.println("[CLIENT] login ok=" + r.isOk()
                    + " user=" + r.getDisplayName()
                    + " role=" + r.getRole()
                    + " reason=" + r.getReason());
            return;
        }
        System.out.println("[CLIENT] unknown message: " + msg.getClass().getName());
    }


    // Tiny demo main so you can run this without touching your UI
    public static void main(String[] args) throws Exception {
        ClientBridge bridge = new ClientBridge("127.0.0.1", 3000);

        // Send a Ping via the client EventBus
        ClientBus.get().post(new SendMessageEvent(new Ping("hello from client")));

        // give it a moment to round-trip
        Thread.sleep(1500);

        // clean exit
        try { bridge.closeConnection(); } catch (Exception ignore) {}
    }
}
