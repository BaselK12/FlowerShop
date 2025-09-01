package il.cshaifasweng.OCSFMediatorExample.server.bus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ServerBus {
    private final Map<Class<?>, List<Consumer<?>>> subs = new ConcurrentHashMap<>();

    public <T> void subscribe(Class<T> type, Consumer<T> handler) {
        subs.computeIfAbsent(type, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(handler);
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        var list = subs.getOrDefault(event.getClass(), List.of());
        synchronized (list) {
            for (var h : list) ((Consumer<T>) h).accept(event);
        }
    }
}
