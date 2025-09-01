package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;

public class OutboundSender {
    public OutboundSender(ServerBus bus) {
        bus.subscribe(SendToClientEvent.class, evt -> {
            try { evt.client().sendToClient(evt.payload()); }
            catch (Exception e) { System.err.println("[SEND] " + e.getMessage()); }
        });
    }
}
