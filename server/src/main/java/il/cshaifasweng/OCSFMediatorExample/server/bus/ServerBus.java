package il.cshaifasweng.OCSFMediatorExample.server.bus;

import com.google.common.eventbus.EventBus;

public final class ServerBus {
    private static final EventBus BUS = new EventBus("server-bus");

    private ServerBus() {}                // no instances, utility holder

    public static EventBus get() {        // global access point
        return BUS;
    }
}
