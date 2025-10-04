package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import java.io.Serializable;

public class SubmitComplaintRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long customerId;   // now Long
    private Long orderId;      // now Long
    private String category;
    private String subject;
    private String message;
    private boolean anonymous;
    private String email;
    private String phone;

    public SubmitComplaintRequest() {}

    public SubmitComplaintRequest(Long customerId, Long orderId, String category,
                                  String subject, String message,
                                  boolean anonymous, String email, String phone) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.category = category;
        this.subject = subject;
        this.message = message;
        this.anonymous = anonymous;
        this.email = email;
        this.phone = phone;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
