package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;
import java.io.Serializable;


public class LoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;
    private final Role role; // default use is a customer

    public LoginRequest(String username, String password) {
        this(username, password, Role.CUSTOMER); // default
    }

    public LoginRequest(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role     = (role != null) ? role : Role.CUSTOMER; // null-safe
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
}
