package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class Pong implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String reply;

    public Pong(String reply) {
        this.reply = reply;
    }
    public String getReply() {
        return reply;
    }
}
