package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

/** Server → Client: result of a login attempt. */
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean ok;          // true if login succeeded
    private final String reason;       // error text when ok=false
    private final String displayName;  // e.g. "Alice Customer"
    private final String role;         // e.g. "CUSTOMER"

    public LoginResponse(boolean ok, String reason, String displayName, String role) {
        this.ok = ok;
        this.reason = reason;
        this.displayName = displayName;
        this.role = role;
    }
    public boolean isOk() { return ok; }
    public String getReason() { return reason; }
    public String getDisplayName() { return displayName; }
    public String getRole() { return role; }
}
