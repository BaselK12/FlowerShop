package il.cshaifasweng.OCSFMediatorExample.client.session;

import il.cshaifasweng.OCSFMediatorExample.entities.dto.CustomerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;

/** Greenrobot EventBus events for session + view lifecycle. */
public final class SessionEvents {
    private SessionEvents() {}

    /** Sticky: posted whenever session changes (login, overview update, logout). */
    public static final class SessionChanged {
        public final boolean loggedIn;
        public final String displayName;
        public final Role role;
        public final CustomerDTO customer;
        public SessionChanged(boolean loggedIn, String displayName, Role role, CustomerDTO customer) {
            this.loggedIn = loggedIn;
            this.displayName = displayName;
            this.role = role;
            this.customer = customer;
        }
    }

    /** Posted when the active controller/view changes. */
    public static final class ActiveControllerChanged {
        public final String controllerId; // e.g., "HomePage", "CatalogView", "Cart"
        public ActiveControllerChanged(String controllerId) { this.controllerId = controllerId; }
    }
}
