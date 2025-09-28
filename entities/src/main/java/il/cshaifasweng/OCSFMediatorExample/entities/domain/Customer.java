package il.cshaifasweng.OCSFMediatorExample.entities.domain;


public class Customer extends Person {

    private static final long serialVersionUID = 1L;

    // You can add customer-specific fields here
    private int loyaltyPoints;

    public Customer() {
        super();
    }

    public Customer(String name, String email, String phone, Gender gender) {
        super(name, email, phone, gender);  // ✅ matches Person constructor
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }
}
