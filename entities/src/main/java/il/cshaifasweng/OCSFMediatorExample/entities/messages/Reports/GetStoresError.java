package il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports;

import java.io.Serializable;

public class GetStoresError implements Serializable {
    public String message;
    public GetStoresError() {}
    public GetStoresError(String message) { this.message = message; }
}
