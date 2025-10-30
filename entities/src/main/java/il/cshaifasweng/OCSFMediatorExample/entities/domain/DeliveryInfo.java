package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "delivery_info",
        indexes = {
                @Index(name = "idx_delivery_order", columnList = "order_id"),
                @Index(name = "idx_delivery_date", columnList = "delivery_date")
        }
)
public class DeliveryInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "street", nullable = false, length = 150)
    private String street;

    @Column(name = "house", nullable = false, length = 20)
    private String house;

    @Column(name = "zip_code", length = 15)
    private String zipCode;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "delivery_time", length = 10)
    private String deliveryTime;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Order order;  // each delivery belongs to one order

    public DeliveryInfo() {}

    public DeliveryInfo(String city, String street, String house, String zipCode,
                        String phone, LocalDate deliveryDate, String deliveryTime) {
        this.city = city;
        this.street = street;
        this.house = house;
        this.zipCode = zipCode;
        this.phone = phone;
        this.deliveryDate = deliveryDate;
        this.deliveryTime = deliveryTime;
    }

    public Long getId() { return id; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getHouse() { return house; }
    public void setHouse(String house) { this.house = house; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}
