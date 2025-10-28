package il.cshaifasweng.OCSFMediatorExample.entities.messages.Admin;

import java.io.Serializable;

public class AdminLoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final String role;

    public AdminLoginResponse(boolean success, String message) {
        this(success, message, "Florist");
    }

    public AdminLoginResponse(boolean success, String message, String role) {
        this.success = success;
        this.message = message;
        this.role = role;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getRole() { return role; }
}
