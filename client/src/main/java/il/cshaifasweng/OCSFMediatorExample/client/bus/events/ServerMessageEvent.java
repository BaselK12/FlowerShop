package il.cshaifasweng.OCSFMediatorExample.client.bus.events;

public class ServerMessageEvent {
    private final Object payload;
    public ServerMessageEvent(Object payload) { this.payload = payload; }
    public Object getPayload() { return payload; }
}


