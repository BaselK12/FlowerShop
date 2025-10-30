package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;


@Entity
@Table(
        name = "pickup_info",
        indexes = {
                @Index(name = "idx_pickup_order", columnList = "order_id"),
                @Index(name = "idx_pickup_date", columnList = "pickup_date")
        }
)
public class PickupInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "branch_name", nullable = false, length = 150)
    private String branchName;

    @Column(name = "pickup_date", nullable = false)
    private LocalDate pickupDate;

    @Column(name = "pickup_time", length = 10)
    private String pickupTime;

    @Column(name = "phone", length = 20)
    private String phone;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Order order;  // each pickup belongs to one order

    public PickupInfo() {}

    public PickupInfo(String branchName, LocalDate pickupDate, String pickupTime, String phone) {
        this.branchName = branchName;
        this.pickupDate = pickupDate;
        this.pickupTime = pickupTime;
        this.phone = phone;
    }

    public Long getId() { return id; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}