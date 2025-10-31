package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.ClientConnectedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.ClientDisconnectedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToAllClientsEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OutboundSender {

    // keep track of all currently connected clients
    private static final Set<ConnectionToClient> clients = ConcurrentHashMap.newKeySet();

    public OutboundSender(ServerBus bus) {

        // (1) Send to one client normally
        bus.subscribe(SendToClientEvent.class, evt -> {
            try {
                evt.client().sendToClient(evt.payload());
            } catch (Exception e) {
                System.err.println("[SEND] Failed: " + e.getMessage());
            }
        });

        // (2) Send to ALL clients
        bus.subscribe(SendToAllClientsEvent.class, evt -> {
            for (var client : clients) {
                try {
                    client.sendToClient(evt.getMessage());
                } catch (Exception e) {
                    System.err.println("[BROADCAST] Failed to send to " + client + ": " + e.getMessage());
                }
            }
        });

        // (3) Subscribe to connection tracking events (optional but ideal)
        bus.subscribe(ClientConnectedEvent.class, evt -> clients.add(evt.client()));
        bus.subscribe(ClientDisconnectedEvent.class, evt -> clients.remove(evt.client()));
    }
}
