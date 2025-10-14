package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;
import java.time.LocalDate;

public class CouponDTO implements Serializable {
    private Long id;
    private String code;
    private String title;
    private String description;
    private LocalDate expiration;
    private boolean active; // computed as !used && expiration >= today

    public CouponDTO() {}

    public CouponDTO(Long id, String code, String title, String description,
                     LocalDate expiration, boolean active) {
        this.id = id; this.code = code; this.title = title; this.description = description;
        this.expiration = expiration; this.active = active;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getExpiration() { return expiration; }
    public boolean isActive() { return active; }
}
