package il.cshaifasweng.OCSFMediatorExample.client.common;

import il.cshaifasweng.OCSFMediatorExample.entities.dto.CustomerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.UpdateCustomerProfileResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.atomic.AtomicReference;

/** Sidecar session cache: no SimpleClient edits needed. */
public final class ClientSession {

    private static final AtomicReference<CustomerDTO> CURRENT = new AtomicReference<>();
    private static volatile boolean installed;
    private static final Object LOCK = new Object();

    // Optional: keep what LoginResponse actually gives you
    private static volatile String loginDisplayName;
    private static volatile Role loginRole;

    private ClientSession() {}

    /** Call once (safe to call repeatedly). */
    public static void install() {
        if (installed) return;
        synchronized (LOCK) {
            if (installed) return;
            EventBus.getDefault().register(new ClientSession());
            installed = true;
        }
    }

    public static void clear() {
        CURRENT.set(null);
        loginDisplayName = null;
        loginRole = null;
    }

    public static CustomerDTO getCustomer() { return CURRENT.get(); }
    public static long getCustomerId() {
        var c = CURRENT.get();
        return c == null ? 0L : c.getId();
    }
    public static String getLoginDisplayName() { return loginDisplayName; }
    public static Role getLoginRole() { return loginRole; }

    /* ===== EventBus subscribers ===== */

    @SuppressWarnings("unused")
    @Subscribe
    public void onLogin(LoginResponse resp) {
        if (resp != null && resp.isOk()) {
            loginDisplayName = resp.getDisplayName();
            loginRole = resp.getRole();
            // No CustomerDTO in your LoginResponse. We'll hydrate after AccountOverview.
        } else {
            clear();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onOverview(AccountOverviewResponse resp) {
        if (resp != null && resp.ok() && resp.customer() != null) {
            CURRENT.set(resp.customer());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onProfileUpdated(UpdateCustomerProfileResponse resp) {
        if (resp != null && resp.ok() && resp.customer() != null) {
            CURRENT.set(resp.customer());
        }
    }
}
