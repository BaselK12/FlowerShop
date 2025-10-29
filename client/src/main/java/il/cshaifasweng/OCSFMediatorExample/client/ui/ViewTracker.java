package il.cshaifasweng.OCSFMediatorExample.client.ui;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

public final class ViewTracker {
    private static volatile String currentId = null;
    private static WeakReference<Object> currentController = new WeakReference<>(null);

    private ViewTracker() {}

    public static void setActive(String controllerId, Object controller) {
        currentId = controllerId;
        currentController = new WeakReference<>(controller);
        EventBus.getDefault().post(new ActiveControllerChanged(controllerId));
    }

    public static String currentId() { return currentId; }
    public static Object currentController() { return currentController.get(); }

    // <<< This is the event your controller is subscribing to >>>
    public static final class ActiveControllerChanged {
        public final String controllerId;
        public ActiveControllerChanged(String controllerId) { this.controllerId = controllerId; }
        // add a getter too, in case you prefer e.getControllerId()
        public String getControllerId() { return controllerId; }
        @Override public String toString() { return "ActiveControllerChanged[" + controllerId + "]"; }
    }
}
