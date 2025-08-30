package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;

import java.io.Serializable;

/** Server → Client: result of a login attempt. */
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean ok;          // true if login succeeded
    private final String reason;       // error text when ok=false
    private final String displayName;  // e.g. "Alice Customer"
    private final Role role;

    public LoginResponse(boolean ok, String reason, String displayName, Role role) {
        this.ok = ok;
        this.reason = reason;
        this.displayName = displayName;
        this.role = role;
    }
    public boolean isOk() { return ok; }
    public String getReason() { return reason; }
    public String getDisplayName() { return displayName; }
    public Role getRole() { return role; }
}
