package il.cshaifasweng.OCSFMediatorExample.client.bus;

import com.google.common.eventbus.EventBus;


public final class ClientBus {
    private static final EventBus BUS = new EventBus("client-bus");

    private ClientBus() {}         // no instances

    public static EventBus get() { // global access to the same bus
        return BUS;
    }
}
