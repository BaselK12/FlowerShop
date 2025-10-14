package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Payment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class PaymentsRepository {
    private PaymentsRepository() {}

    private static final Map<Long, List<Payment>> BY_CUSTOMER = new ConcurrentHashMap<>();
    private static final AtomicInteger SEQ = new AtomicInteger(1000);

    public static List<Payment> findByCustomer(long customerId) {
        return new ArrayList<>(BY_CUSTOMER.getOrDefault(customerId, List.of()));
    }

    public static Payment add(long customerId, Payment p) {
        if (p.getId() == null || p.getId().isBlank()) {
            p.setId("PAY" + SEQ.incrementAndGet());
        }
        if (p.getStatus() == null) {
            p.setStatus(Payment.Status.AUTHORIZED); // stored method OK
        }
        BY_CUSTOMER.computeIfAbsent(customerId, k -> new ArrayList<>()).add(p);
        return p;
    }

    public static boolean remove(long customerId, String paymentId) {
        var list = BY_CUSTOMER.get(customerId);
        if (list == null) return false;
        return list.removeIf(p -> Objects.equals(p.getId(), paymentId));
    }
}
