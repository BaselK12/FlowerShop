package il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard;

import java.io.Serializable;

public class DeleteFlowerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sku;

    public DeleteFlowerRequest(String sku) {
        this.sku = sku;
    }

    /** No-args constructor for serialization frameworks */
    public DeleteFlowerRequest() {}

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    @Override
    public String toString() {
        return "DeleteFlowerRequest{sku='" + sku + "'}";
    }
}
