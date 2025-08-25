package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import com.google.common.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Pong;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.PingReceived;


public class PingHandler {

    @Subscribe
    public void onPing(PingReceived ev) {
        try {
            ev.client.sendToClient(new Pong("pong: " + ev.text));
        } catch (Exception e) {
            e.printStackTrace(); // microscopic demo = microscopic error handling
        }
    }
}
