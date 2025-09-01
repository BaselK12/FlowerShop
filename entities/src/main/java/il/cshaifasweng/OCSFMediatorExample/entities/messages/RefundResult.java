package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class RefundResult implements Serializable {
    public enum Tier { FULL, HALF, NONE }

    private Tier tier;
    private double amount;

    public RefundResult() {}
    public RefundResult(Tier tier, double amount) {
        this.tier = tier; this.amount = amount;
    }

    public Tier getTier() { return tier; }
    public void setTier(Tier tier) { this.tier = tier; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
