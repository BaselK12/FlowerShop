package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import java.io.Serializable;
import java.util.Objects;

public class ComplaintDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String category;
    private String orderId;   // optional
    private String subject;
    private String message;
    private boolean anonymous;
    private String email;     // null if anonymous
    private String phone;     // null if anonymous

    // no-arg constructor required by Java serialization
    public ComplaintDTO() {}

    // convenience all-args constructor (optional)
    public ComplaintDTO(String category, String orderId, String subject, String message,
                        boolean anonymous, String email, String phone) {
        this.category = category;
        this.orderId = orderId;
        this.subject = subject;
        this.message = message;
        this.anonymous = anonymous;
        this.email = email;
        this.phone = phone;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

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

    // optional but handy for logging/debugging (omit message body to avoid log noise)
    @Override
    public String toString() {
        return "ComplaintDTO{" +
                "category='" + category + '\'' +
                ", orderId='" + orderId + '\'' +
                ", subject='" + subject + '\'' +
                ", anonymous=" + anonymous +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    // optional equals/hashCode (useful in tests)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComplaintDTO)) return false;
        ComplaintDTO that = (ComplaintDTO) o;
        return anonymous == that.anonymous &&
                Objects.equals(category, that.category) &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(message, that.message) &&
                Objects.equals(email, that.email) &&
                Objects.equals(phone, that.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, orderId, subject, message, anonymous, email, phone);
    }
}
