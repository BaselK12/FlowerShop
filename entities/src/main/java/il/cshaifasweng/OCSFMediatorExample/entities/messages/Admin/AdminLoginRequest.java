package il.cshaifasweng.OCSFMediatorExample.entities.messages.Admin;

import java.io.Serializable;

public class AdminLoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    public AdminLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
