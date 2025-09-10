package il.cshaifasweng.OCSFMediatorExample.server.model;

public final class ModelRegistry {
    private ModelRegistry() {}
    public static Class<?>[] entities() {
        return new Class<?>[] {
                Employee.class
                // Department.class, Order.class, Product.class, Customer.class, ...
        };
    }
}
