package il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut;

import java.io.Serializable;
import java.time.LocalDate;

public class PickupInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String branchName;   // user-facing shop name
    private LocalDate pickupDate;
    private String pickupTime;   // "HH:mm"
    private String phone;

    public PickupInfoDTO() {}

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
