package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

public class SendToAllClientsEvent {

    private final Object message;

    public SendToAllClientsEvent(Object message) {
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "SendToAllClientsEvent{ message=" + message + " }";
    }
}