package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;


public class Ping implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String text;

    public Ping(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
