package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;

public class GreetingCard implements Serializable {
    private String message;
    private String from;
    private String to;

    public GreetingCard() {}

    public GreetingCard(String message, String from, String to) {
        this.message = message;
        this.from = from;
        this.to = to;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
}
