package il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet;

import java.io.Serializable;

public class AddCustomBouquetResponse implements Serializable {
    private final boolean success;
    private final String message;

    public AddCustomBouquetResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "AddCustomBouquetResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
