package il.cshaifasweng.OCSFMediatorExample.client.session;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.dto.CustomerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.UpdateCustomerProfileResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

/** Minimal session holder for the logged-in customer on the client side. */
public final class ClientSession {
    private static volatile boolean installed = false;
    private static volatile long customerId = 0;
    private static volatile String displayName = null;
    private static volatile boolean loggedIn = false;
    private static volatile Role role = null;
    private static volatile CustomerDTO customer = null;


    private ClientSession() {}

    /** Register a singleton subscriber once. Safe to call repeatedly. */
    public static synchronized void install() {
        if (installed) return;
        EventBus.getDefault().register(new ClientSession());
        postSticky();
        installed = true;
    }

    public static long getCustomerId() { return customerId; }
    public static String getDisplayName() { return displayName; }
    public static boolean isLoggedIn() { return loggedIn; }
    public static Role getRole() { return role; }
    public static CustomerDTO getCustomer() { return customer; }

    // Convenience snapshot if you like having one thing to pass around
    public static Snapshot getSnapshot() {
        return new Snapshot(loggedIn, customerId, displayName, role, customer);
    }

    /** Sticky session event every controller can subscribe to. */
    public static final class SessionChanged {
        public final boolean loggedIn;
        public final long customerId;
        public final String displayName;
        public final Role role;
        public final CustomerDTO customer;
        public SessionChanged(boolean loggedIn, long customerId, String displayName, Role role, CustomerDTO customer) {
            this.loggedIn = loggedIn;
            this.customerId = customerId;
            this.displayName = displayName;
            this.role = role;
            this.customer = customer;
        }
    }

    /** Immutable view of the session for convenience. */
    public static final class Snapshot {
        public final boolean loggedIn;
        public final long customerId;
        public final String displayName;
        public final Role role;
        public final CustomerDTO customer;
        public Snapshot(boolean loggedIn, long customerId, String displayName, Role role, CustomerDTO customer) {
            this.loggedIn = loggedIn;
            this.customerId = customerId;
            this.displayName = displayName;
            this.role = role;
            this.customer = customer;
        }
    }


    public static void clear() {
        customerId = 0;
        displayName = null;
        loggedIn = false;
        role = null;
        customer = null;
        postSticky(); // NEW
    }

    private static void postSticky() {
        EventBus.getDefault().postSticky(
                new SessionChanged(loggedIn, customerId, displayName, role, customer)
        );
    }

    @Subscribe
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk()) return;
        var c = r.getCustomer();
        if (c == null) return;

        customerId  = c.getId();
        displayName = c.getDisplayName();
        customer   = c;
        loggedIn   = true;
        postSticky();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) throws IOException {
        if (r == null) return;

        if (!r.isOk()) {
            clear();
            return;
        }
        // Partial info first so UI updates immediately
        displayName = r.getDisplayName();
        role = r.getRole();
        loggedIn = true;

        postSticky(); // publish partial snapshot now

        // Ask server for full customer payload (server can infer from connection when id=0)
        SimpleClient.getClient().sendToServer(new AccountOverviewRequest(0));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProfileUpdated(UpdateCustomerProfileResponse r) {
        if (r == null || !r.ok() || r.customer() == null) return;
        customer = r.customer();
        displayName = r.customer().getDisplayName();
        // customerId unchanged
        postSticky();
    }



}
