package il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;

import java.io.Serializable;

public class SaveFlowerResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String error;
    private FlowerDTO updatedFlower; // optional: returned if success = true

    public SaveFlowerResponse(boolean success, String error, FlowerDTO updatedFlower) {
        this.success = success;
        this.error = error;
        this.updatedFlower = updatedFlower;
    }

    public SaveFlowerResponse(boolean success, String error) {
        this(success, error, null);
    }

    public SaveFlowerResponse() {}

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

    public FlowerDTO getUpdatedFlower() {
        return updatedFlower;
    }

    public void setUpdatedFlower(FlowerDTO updatedFlower) {
        this.updatedFlower = updatedFlower;
    }

    @Override
    public String toString() {
        return "SaveFlowerResponse{" +
                "success=" + success +
                ", error='" + error + '\'' +
                ", updatedFlower=" + (updatedFlower != null ? updatedFlower.getSku() : "null") +
                '}';
    }
}
