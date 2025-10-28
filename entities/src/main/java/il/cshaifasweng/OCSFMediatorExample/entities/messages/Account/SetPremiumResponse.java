package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class SetPremiumResponse implements Serializable {
    private boolean ok;
    private String error;

    public SetPremiumResponse() { }

    private SetPremiumResponse(boolean ok, String error) {
        this.ok = ok;
        this.error = error;
    }

    public static SetPremiumResponse ok() { return new SetPremiumResponse(true, null); }
    public static SetPremiumResponse fail(String err) { return new SetPremiumResponse(false, err); }

    public boolean isOk() { return ok; }
    public String getError() { return error; }

    public void setOk(boolean ok) { this.ok = ok; }
    public void setError(String error) { this.error = error; }
}
