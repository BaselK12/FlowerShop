package il.cshaifasweng.OCSFMediatorExample.client.session;

import il.cshaifasweng.OCSFMediatorExample.entities.dto.CustomerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;

/** Immutable snapshot of the current client-side session. */
public record SessionSnapshot(
        boolean loggedIn,
        String displayName,   // from LoginResponse or CustomerDTO
        Role role,            // from LoginResponse
        CustomerDTO customer  // from AccountOverviewResponse (may be null right after login)
) {
    public long customerId() {
        return customer != null ? customer.getId() : 0L;
    }
}
