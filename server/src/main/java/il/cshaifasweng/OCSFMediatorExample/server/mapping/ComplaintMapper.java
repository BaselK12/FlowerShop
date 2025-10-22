package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.*;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintRequest;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Builds a Complaint using mapped entities. Validates:
 *  - customer exists
 *  - if orderId provided: order exists AND belongs to same customer
 * Derives store from order.store_id; otherwise uses defaultStoreId.
 *
 * Works whether Complaint uses scalar FK setters (setXId) or associations (setX(entity)).
 */
public final class ComplaintMapper {

    public static Complaint fromRequest(Session s, SubmitComplaintRequest req, long defaultStoreId) {
        if (req == null) throw new IllegalArgumentException("Empty request.");
        if (req.getCustomerId() == null) throw new IllegalArgumentException("Missing customer.");

        // Customer must exist
        Customer customer = s.get(Customer.class, req.getCustomerId());
        if (customer == null) throw new IllegalArgumentException("Customer not found.");

        // Resolve and validate order if present
        Order order = null;
        if (req.getOrderId() != null) {
            order = s.get(Order.class, req.getOrderId());
            if (order == null) throw new IllegalArgumentException("Order not found.");
            if (!Objects.equals(order.getCustomerId(), req.getCustomerId())) {
                throw new IllegalArgumentException("Order does not belong to your account.");
            }
        }

        // Derive store id
        Long storeId = (order != null && order.getStoreId() != null)
                ? order.getStoreId()
                : defaultStoreId;

        // Fetch Stores entity if the Complaint expects an association
        Stores storeEntity = null;
        // we’ll only fetch if we need to set the association below

        Complaint c = new Complaint();

        // Optional timestamps/status (ignore if your entity doesn't have these setters)
        try { c.getClass().getMethod("setCreatedAt", LocalDateTime.class).invoke(c, LocalDateTime.now()); } catch (Throwable ignored) {}
        try { c.getClass().getMethod("setStatus", Complaint.Status.class).invoke(c, Complaint.Status.OPEN); } catch (Throwable ignored) {}
        try { c.getClass().getMethod("setResolution", String.class).invoke(c, (String) null); } catch (Throwable ignored) {}

        // Prefer scalar FK setters if present
        boolean setCustomerId = tryCall(c, "setCustomerId", Long.class, customer.getId());
        boolean setOrderId    = tryCall(c, "setOrderId",    Long.class, req.getOrderId());
        boolean setStoreId    = tryCall(c, "setStoreId",    Long.class, storeId);

        // Fall back to associations if needed
        if (!setCustomerId) tryCall(c, "setCustomer", Customer.class, customer);
        if (!setOrderId && order != null) tryCall(c, "setOrder", Order.class, order);
        if (!setStoreId) {
            if (storeEntity == null) storeEntity = s.get(Stores.class, storeId);
            tryCall(c, "setStore", Stores.class, storeEntity);
        }

        // Content fields: subject, description/text, category/type
        tryCall(c, "setSubject", String.class, req.getSubject());
        if (!tryCall(c, "setDescription", String.class, req.getMessage())) {
            tryCall(c, "setText", String.class, req.getMessage());
        }
        if (!tryCall(c, "setType", String.class, req.getCategory())) {
            tryCall(c, "setCategory", String.class, req.getCategory());
        }

        // Contact/flags
        tryCall(c, "setAnonymous", boolean.class, req.isAnonymous());
        tryCall(c, "setEmail",     String.class,  req.getEmail());
        tryCall(c, "setPhone",     String.class,  req.getPhone());

        return c;
    }

    private static boolean tryCall(Object target, String method, Class<?> param, Object arg) {
        try { target.getClass().getMethod(method, param).invoke(target, arg); return true; }
        catch (Throwable ignored) { return false; }
    }

    private ComplaintMapper() {}
}
