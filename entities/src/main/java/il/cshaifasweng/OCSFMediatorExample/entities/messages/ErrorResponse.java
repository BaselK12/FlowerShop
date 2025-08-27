package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class ErrorResponse implements Serializable {
    private final String reason;

    public ErrorResponse(String reason) {
        this.reason = reason;
    }

    public String reason() {
        return reason;
    }
}