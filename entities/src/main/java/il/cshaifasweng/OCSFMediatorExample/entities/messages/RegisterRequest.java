package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;     // email
    private final String password;     // plain for demo (hash on server)
    private final String displayName;  // full name
    private final String phone;        // optional

    // New:
    private final Long storeId;        // null means Global Account
    private final boolean premium;     // true if user opted in and “paid”

    public RegisterRequest(String username, String password, String displayName) {
        this(username, password, displayName, null, null, false);
    }

    public RegisterRequest(String username, String password, String displayName, String phone) {
        this(username, password, displayName, phone, null, false);
    }

    public RegisterRequest(String username, String password, String displayName, String phone,
                           Long storeId, boolean premium) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.phone = phone;
        this.storeId = storeId;
        this.premium = premium;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getDisplayName() { return displayName; }
    public String getPhone() { return phone; }
    public Long getStoreId() { return storeId; }     // null => global
    public boolean isPremium() { return premium; }
}
