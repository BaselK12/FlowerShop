package il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet;

import java.io.Serializable;
import java.util.Map;

public class AddCustomBouquetRequest implements Serializable {
    private double totalPrice;
    private Map<String, Integer> flowerComposition; // flower name -> quantity

    public AddCustomBouquetRequest(double totalPrice, Map<String, Integer> flowerComposition) {
        this.totalPrice = totalPrice;
        this.flowerComposition = flowerComposition;
    }

    public double getTotalPrice() { return totalPrice; }
    public Map<String, Integer> getFlowerComposition() { return flowerComposition; }
}