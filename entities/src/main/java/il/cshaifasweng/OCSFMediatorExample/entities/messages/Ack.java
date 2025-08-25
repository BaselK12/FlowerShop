package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class Ack implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String message;
    public Ack(String message) { this.message = message; }
    public String getMessage() { return message; }
}
