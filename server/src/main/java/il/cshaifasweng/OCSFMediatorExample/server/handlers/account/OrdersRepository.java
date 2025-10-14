package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class OrdersRepository {
    private OrdersRepository() {}

    private static final Map<Long, List<Order>> BY_CUSTOMER = new ConcurrentHashMap<>();
    private static final AtomicLong ORDER_SEQ = new AtomicLong(1000);

    public static void add(Order o) {
        if (o.getId() == null) {
            o.setId(ORDER_SEQ.incrementAndGet());
        }
        if (o.getCreatedAt() == null) {
            o.setCreatedAt(LocalDateTime.now());
        }
        BY_CUSTOMER.computeIfAbsent(o.getCustomerId(), k -> new ArrayList<>()).add(o);
    }

    public static List<Order> findByCustomer(long customerId) {
        var list = new ArrayList<>(BY_CUSTOMER.getOrDefault(customerId, List.of()));
        list.sort(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return list;
    }
}
