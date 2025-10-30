package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false, length = 64, unique = true)
    private String code;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(length = 512)
    private String description;

    @Column(nullable = false)
    private LocalDate expiration;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Coupon() {}

    public Coupon(Customer customer, String code, String title,
                  String description, LocalDate expiration, boolean used) {
        this.customer = customer;
        this.code = code;
        this.title = title;
        this.description = description;
        this.expiration = expiration;
        this.used = used;
    }

    @Transient
    public long daysRemaining() {
        return expiration != null ? LocalDate.now().until(expiration).getDays() : 0;
    }

    // getters/setters
    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getExpiration() { return expiration; }
    public void setExpiration(LocalDate expiration) { this.expiration = expiration; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public boolean isActiveToday() {
        return !used && (expiration == null || !expiration.isBefore(LocalDate.now()));
    }
}
