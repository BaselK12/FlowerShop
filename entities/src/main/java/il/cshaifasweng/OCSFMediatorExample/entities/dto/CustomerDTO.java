package il.cshaifasweng.OCSFMediatorExample.entities.dto;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Customer;
import java.io.Serializable;

public class CustomerDTO implements Serializable {
    private final long id;
    private final String displayName;
    private final String email;
    private final String phone;
    private final boolean active;

    public CustomerDTO(long id, String displayName, String email, String phone, boolean active) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.active = active;
    }

    public long getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }

    public static CustomerDTO from(Customer c) {
        return new CustomerDTO(
                c.getId(),
                c.getDisplayName(),
                c.getEmail(),
                c.getPhone(),
                c.isActive()
        );
    }
}