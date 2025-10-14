package il.cshaifasweng.OCSFMediatorExample.client.common;

public final class Session {
    private static long customerId = 0;

    private Session() {}

    public static void setCustomerId(long id) { customerId = id; }

    public static long getCustomerId() { return customerId; }

    public static void clear() { customerId = 0; }
}
