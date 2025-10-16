package il.cshaifasweng.OCSFMediatorExample.server.model;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.*;

public final class ModelRegistry {
    private ModelRegistry() {}
    public static Class<?>[] entities() {
        return new Class<?>[] {
                Employee.class,
                Complaint.class,
                Category.class,
                Flower.class,
                Promotion.class,
                Stores.class,
                Customer.class,
                Coupon.class,
                Payment.class,
                CartItemRow.class
                // Department.class, Order.class, Product.class, Customer.class, ...
        };
    }
}
