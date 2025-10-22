package il.cshaifasweng.OCSFMediatorExample.client.session;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;

/** Minimal session holder for the logged-in customer on the client side. */
public final class ClientSession {
    private static volatile boolean installed = false;
    private static volatile long customerId = 0;
    private static volatile String displayName = null;

    private ClientSession() {}

    /** Register a singleton subscriber once. Safe to call repeatedly. */
    public static synchronized void install() {
        if (installed) return;
        EventBus.getDefault().register(new ClientSession());
        installed = true;
    }

    public static long getCustomerId() { return customerId; }
    public static String getDisplayName() { return displayName; }

    public static void clear() {
        customerId = 0;
        displayName = null;
    }

    @Subscribe
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk()) return;
        var c = r.getCustomer();
        if (c == null) return;

        customerId  = c.getId();
        displayName = c.getDisplayName();
    }
}
