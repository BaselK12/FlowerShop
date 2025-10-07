package il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut;

import java.io.Serializable;

public class GreetingCardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;
    private String senderName;
    private String recipientName;
    private String recipientPhone;

    public GreetingCardDTO() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
}
