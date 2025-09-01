package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PickupInfo implements Serializable {
    private String shopId;
    private String contactName;
    private String phone;
    private LocalDateTime scheduledAt;

    public PickupInfo() {}

    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
}
