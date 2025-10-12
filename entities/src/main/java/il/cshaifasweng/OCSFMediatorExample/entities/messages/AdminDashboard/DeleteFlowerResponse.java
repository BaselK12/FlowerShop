package il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard;

import java.io.Serializable;

public class DeleteFlowerResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String error;

    public DeleteFlowerResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    /** No-args constructor for serialization frameworks */
    public DeleteFlowerResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "DeleteFlowerResponse{success=" + success +
                ", error='" + error + "'}";
    }
}
