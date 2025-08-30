package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;     // email or username
    private final String password;     // plain for class demo (hash on server)
    private final String displayName;  // full name to show in UI
    private final String phone;        // optional

    public RegisterRequest(String username, String password, String displayName) {
        this(username, password, displayName, null);
    }

    public RegisterRequest(String username, String password, String displayName, String phone) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.phone = phone;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getDisplayName() { return displayName; }
    public String getPhone() { return phone; }
}

