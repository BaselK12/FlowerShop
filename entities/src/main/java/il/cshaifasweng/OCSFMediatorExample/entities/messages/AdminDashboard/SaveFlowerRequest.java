package il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard;

import java.io.Serializable;
import java.util.List;

public class SaveFlowerRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ActionType { ADD, EDIT }

    private ActionType action;
    private String sku;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private List<String> categories;

    public SaveFlowerRequest(ActionType action, String sku, String name, String description,
                             double price, String imageUrl, List<String> categories) {
        this.action = action;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.categories = categories;
    }

    public SaveFlowerRequest() {}

    public ActionType getAction() { return action; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getCategories() { return categories; }

    public void setAction(ActionType action) { this.action = action; }
    public void setSku(String sku) { this.sku = sku; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    @Override
    public String toString() {
        return "SaveFlowerRequest{" +
                "action=" + action +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", categories=" + categories +
                '}';
    }
}