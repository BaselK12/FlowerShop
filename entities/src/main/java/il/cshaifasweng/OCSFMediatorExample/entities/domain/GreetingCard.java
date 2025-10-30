package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import jakarta.persistence.*;



@Entity
@Table(
        name = "greeting_cards",
        indexes = {
                @Index(name = "idx_greeting_order", columnList = "order_id")
        }
)
public class GreetingCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "sender_name", nullable = false, length = 100)
    private String senderName;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Order order;   // one greeting card per order

    public GreetingCard() {}

    public GreetingCard(String message, String senderName, String recipientName,
                        String recipientPhone, Order order) {
        this.message = message;
        this.senderName = senderName;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.order = order;
    }

    public Long getId() { return id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}
