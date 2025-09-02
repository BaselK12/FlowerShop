package il.cshaifasweng.OCSFMediatorExample.entities.domain;

public class Customer extends Person {

    public Customer() {
        super();
    }

    public Customer(String id, String firstName, String lastName, String email, String phone) {
        super(id, firstName, lastName, email, phone);
    }
}
