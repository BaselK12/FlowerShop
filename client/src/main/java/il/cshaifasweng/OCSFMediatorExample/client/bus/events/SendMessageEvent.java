package il.cshaifasweng.OCSFMediatorExample.client.bus.events;


public class SendMessageEvent {
    public final Object payload;
    public SendMessageEvent(Object payload) { this.payload = payload; }
}
