package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.RegisterRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.RegisterResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.RegisterRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Locale;

public class RegisterHandler {
    private final ServerBus bus;

    public RegisterHandler(ServerBus bus) {
        this.bus = bus;
        bus.subscribe(RegisterRequestedEvent.class, this::onRegisterRequested);
        System.out.println("[SRV/RegisterHandler] subscribed");
    }

    private void onRegisterRequested(RegisterRequestedEvent evt) {
        RegisterRequest req = evt.req();
        System.out.println("[SRV/RegisterHandler] start email=" + req.getUsername());

        try {
            // --- basic validation ---
            if (req.getUsername() == null || !req.getUsername().contains("@")) {
                System.out.println("[SRV/RegisterHandler] invalid email");
                send(evt, RegisterResponse.error("Invalid email"));
                return;
            }
            if (req.getDisplayName() == null || req.getDisplayName().trim().isEmpty()) {
                System.out.println("[SRV/RegisterHandler] missing name");
                send(evt, RegisterResponse.error("Name is required"));
                return;
            }
            if (req.getPassword() == null || req.getPassword().length() < 6) {
                System.out.println("[SRV/RegisterHandler] short password");
                send(evt, RegisterResponse.error("Password must be at least 6 characters"));
                return;
            }

            // --- duplicate check ---
            boolean exists = TX.call(s -> {
                System.out.println("[SRV/RegisterHandler] checking duplicate for " + req.getUsername());
                Long n = s.createQuery(
                                "select count(c.id) from Customer c where c.email = :email", Long.class)
                        .setParameter("email", req.getUsername().toLowerCase(Locale.ROOT))
                        .uniqueResult();
                return n != null && n > 0;
            });
            System.out.println("[SRV/RegisterHandler] duplicate? " + exists);
            if (exists) {
                send(evt, RegisterResponse.error("Email already registered"));
                return;
            }

            // --- persist ---
            TX.run(s -> {
                System.out.println("[SRV/RegisterHandler] persisting customer " + req.getUsername());
                Customer c = new Customer();
                c.setEmail(req.getUsername().toLowerCase(Locale.ROOT));
                c.setPasswordHash(req.getPassword()); // hash later if/when you implement it
                c.setDisplayName(req.getDisplayName().trim());
                c.setPhone(req.getPhone());
                c.setActive(true);

                // Optional: set branch/global depending on what your Customer supports.
                // Tries these in order:
                //  1) setStoreId(Long)
                //  2) setStore(Store)  where Store entity is ...domain.Store or ...domain.Stores
                Long storeId = req.getStoreId();
                if (storeId != null) {
                    boolean storeApplied = false;

                    // 1) setStoreId(Long)
                    storeApplied = tryInvoke(c, "setStoreId", new Class<?>[]{Long.class}, new Object[]{storeId});

                    // 2) setStore(Store/Stores) via reflection, if entity exists
                    if (!storeApplied) {
                        try {
                            Class<?> storeClass = tryLoad(
                                    "il.cshaifasweng.OCSFMediatorExample.entities.domain.Store",
                                    "il.cshaifasweng.OCSFMediatorExample.entities.domain.Stores"
                            );
                            if (storeClass != null) {
                                Object store = s.get(storeClass, storeId);
                                if (store != null) {
                                    // find any setStore(*) and call it
                                    Method m = findSingleArgMethod(c.getClass(), "setStore");
                                    if (m != null) {
                                        m.invoke(c, store);
                                        storeApplied = true;
                                    }
                                }
                            }
                        } catch (Exception ignore) {
                            // not the end of the world; account will be Global if we can't bind a store
                        }
                    }
                    System.out.println("[SRV/RegisterHandler] store applied = " + storeApplied + " (id=" + storeId + ")");
                }

                // Optional: premium flags if your Customer has them
                if (req.isPremium()) {
                    boolean premiumApplied = tryInvoke(c, "setPremium", new Class<?>[]{boolean.class}, new Object[]{true});
                    boolean premiumSinceApplied = tryInvoke(c, "setPremiumSince", new Class<?>[]{Instant.class}, new Object[]{Instant.now()});
                    System.out.println("[SRV/RegisterHandler] premium applied = " + (premiumApplied || premiumSinceApplied));
                }

                s.persist(c);
                System.out.println("[SRV/RegisterHandler] persist done (id assigned on flush)");
            });

            System.out.println("[SRV/RegisterHandler] success " + req.getUsername());
            send(evt, RegisterResponse.success());

        } catch (Exception ex) {
            ex.printStackTrace();
            send(evt, RegisterResponse.error("Server error"));
        }
    }

    private void send(RegisterRequestedEvent evt, RegisterResponse r) {
        System.out.println("[SRV/RegisterHandler] replying -> " + (r.isOk() ? "OK" : r.getReason()));
        bus.publish(new SendToClientEvent(r, evt.client()));
    }

    // ---------- tiny reflection helpers so this compiles whether or not you've added fields ----------

    private static boolean tryInvoke(Object target, String methodName, Class<?>[] argTypes, Object[] args) {
        try {
            Method m = target.getClass().getMethod(methodName, argTypes);
            m.invoke(target, args);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception e) {
            System.out.println("[SRV/RegisterHandler] " + methodName + " failed: " + e.getMessage());
            return false;
        }
    }

    private static Class<?> tryLoad(String... fqcnCandidates) {
        for (String name : fqcnCandidates) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignore) { }
        }
        return null;
    }

    private static Method findSingleArgMethod(Class<?> type, String name) {
        for (Method m : type.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1) {
                return m;
            }
        }
        return null;
    }
}
