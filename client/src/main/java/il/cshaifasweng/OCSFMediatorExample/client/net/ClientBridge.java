package il.cshaifasweng.OCSFMediatorExample.client.net;

import com.google.common.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.client.bus.ClientBus;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.SendMessageEvent;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.ServerMessageEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;

public class ClientBridge extends AbstractClient {

    public ClientBridge(String host, int port) {
        super(host, port);
        ClientBus.get().register(this);
        try {
            openConnection(); // connect once at startup
        } catch (Exception e) {
            System.err.println("[ClientBridge] Failed to connect: " + e.getMessage());
        }
    }

    @Subscribe
    public void onSend(SendMessageEvent ev) {
        try {
            if (!isConnected()) {
                openConnection();
            }
            sendToServer(ev.payload);
        } catch (Exception e) {
            System.err.println("[ClientBridge] send failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        // Fan-out to the whole client via the bus
        ClientBus.get().post(new ServerMessageEvent(msg));
    }

    // optional convenience API if you don’t want to post SendMessageEvent everywhere
    public void send(Object any) {
        try {
            if (!isConnected()) openConnection();
            sendToServer(any);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send to server", e);
        }
    }

    // if you really want a CLI sanity check, at least fix the port
    public static void main(String[] args) throws Exception {
        ClientBridge bridge = new ClientBridge("127.0.0.1", 3050);
        Thread.sleep(1500);
        bridge.closeConnection();
    }
}
