package il.cshaifasweng.OCSFMediatorExample.entities.messages;


import java.io.Serializable;

public class RegisterResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean ok;
    private final String reason;  // non-null when ok=false

    public RegisterResponse(boolean ok, String reason) {
        this.ok = ok;
        this.reason = reason;
    }

    public static RegisterResponse success() { return new RegisterResponse(true, null); }
    public static RegisterResponse error(String reason) { return new RegisterResponse(false, reason); }

    public boolean isOk() { return ok; }
    public String getReason() { return reason; }
}
