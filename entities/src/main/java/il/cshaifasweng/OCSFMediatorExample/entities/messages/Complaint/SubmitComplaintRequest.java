package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import java.io.Serializable;

public class SubmitComplaintRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String customerId;   // optional if anonymous
    private String orderId;      // optional
    private String category;
    private String subject;
    private String message;
    private boolean anonymous;
    private String email;        // null if anonymous
    private String phone;        // null if anonymous

    public SubmitComplaintRequest() {}

    public SubmitComplaintRequest(String customerId, String orderId, String category,
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

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

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
