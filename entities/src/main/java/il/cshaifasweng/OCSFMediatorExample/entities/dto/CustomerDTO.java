package il.cshaifasweng.OCSFMediatorExample.entities.dto;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Stores;

import java.io.Serializable;
import java.time.Instant;

public class CustomerDTO implements Serializable {
    private final long id;
    private final String displayName;
    private final String email;
    private final String phone;
    private final boolean active;

    // NEW: account type / premium details
    private final Long storeId;         // null => Global
    private final String storeName;     // null => Global
    private final boolean premium;
    private final Instant premiumSince; // null if not premium

    public CustomerDTO(
            long id,
            String displayName,
            String email,
            String phone,
            boolean active,
            Long storeId,
            String storeName,
            boolean premium,
            Instant premiumSince
    ) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.active = active;
        this.storeId = storeId;
        this.storeName = storeName;
        this.premium = premium;
        this.premiumSince = premiumSince;
    }

    // existing getters (unchanged)
    public long getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }

    // NEW getters
    public Long getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public boolean isPremium() { return premium; }
    public Instant getPremiumSince() { return premiumSince; }

    public static CustomerDTO from(Customer c) {
        Stores s = c.getStore();
        Long sid = (s == null) ? null : s.getId();
        String sname = (s == null) ? null : s.getName();

        return new CustomerDTO(
                c.getId(),
                c.getDisplayName(),
                c.getEmail(),
                c.getPhone(),
                c.isActive(),
                sid,
                sname,
                c.isPremium(),
                c.getPremiumSince()
        );
    }
}
