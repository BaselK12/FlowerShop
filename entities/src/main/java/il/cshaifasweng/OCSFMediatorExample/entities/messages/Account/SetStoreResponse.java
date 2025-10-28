package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class SetStoreResponse implements Serializable {
    private final boolean ok;
    private final String error;

    private SetStoreResponse(boolean ok, String error) {
        this.ok = ok;
        this.error = error;
    }

    public static SetStoreResponse ok() { return new SetStoreResponse(true, null); }
    public static SetStoreResponse fail(String err) { return new SetStoreResponse(false, err); }

    public boolean isOk() { return ok; }
    public String getError() { return error; }
}
