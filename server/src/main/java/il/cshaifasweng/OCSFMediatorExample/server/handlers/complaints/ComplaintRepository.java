package il.cshaifasweng.OCSFMediatorExample.server.handlers.complaints;
import il.cshaifasweng.OCSFMediatorExample.server.session.HbBoot;
import org.hibernate.Session;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ComplaintRepository {

    /**
     * Fetch complaints with optional filters.
     * If a filter is null, it is ignored.
     *
     * @param status     Complaint status (OPEN, IN_PROGRESS, …) or null
     * @param type       Complaint type ("Delivery", "Service", …) or null
     * @param storeId    Store ID or null
     * @param customerId Customer ID or null
     * @param orderId    Order ID or null
     * @param from       Start date range or null
     * @param to         End date range or null
     * @param q          Free text search in complaint text or null
     * @param sortBy     Field to sort by (createdAt, status, type, …)
     * @param sortDesc   true = DESC, false = ASC
     * @param page       Page index (0-based)
     * @param pageSize   Number of results per page
     * @return List of matching complaints
     */
    public List<Complaint> find(
            Complaint.Status status,
            String type,
            Long storeId,
            Long customerId,
            Long orderId,
            LocalDateTime from,
            LocalDateTime to,
            String q,
            String sortBy,
            boolean sortDesc,
            int page,
            int pageSize
    ) {
        StringBuilder hql = new StringBuilder("from Complaint c where 1=1");
        List<Param> params = new ArrayList<>();

        if (status != null) {
            hql.append(" and c.status = :status");
            params.add(new Param("status", status));
        }
        if (type != null && !type.isBlank()) {
            hql.append(" and c.type = :type");
            params.add(new Param("type", type));
        }
        if (storeId != null) {
            hql.append(" and c.storeId = :storeId");
            params.add(new Param("storeId", storeId));
        }
        if (customerId != null) {
            hql.append(" and c.customerId = :customerId");
            params.add(new Param("customerId", customerId));
        }
        if (orderId != null) {
            hql.append(" and c.orderId = :orderId");
            params.add(new Param("orderId", orderId));
        }
        if (from != null && to != null) {
            hql.append(" and c.createdAt between :from and :to");
            params.add(new Param("from", from));
            params.add(new Param("to", to));
        } else if (from != null) {
            hql.append(" and c.createdAt >= :from");
            params.add(new Param("from", from));
        } else if (to != null) {
            hql.append(" and c.createdAt <= :to");
            params.add(new Param("to", to));
        }
        if (q != null && !q.isBlank()) {
            hql.append(" and lower(c.text) like :q");
            params.add(new Param("q", "%" + q.toLowerCase() + "%"));
        }

        // Sorting whitelist
        String sortField = switch (sortBy == null ? "createdAt" : sortBy) {
            case "status" -> "c.status";
            case "type" -> "c.type";
            case "storeId" -> "c.storeId";
            case "customerId" -> "c.customerId";
            case "orderId" -> "c.orderId";
            case "createdAt" -> "c.createdAt";
            default -> "c.createdAt";
        };
        hql.append(" order by ").append(sortField).append(sortDesc ? " desc" : " asc");

        try (Session session = HbBoot.sf().openSession()) {
            Query<Complaint> query = session.createQuery(hql.toString(), Complaint.class);
            for (Param p : params) {
                query.setParameter(p.name, p.value);
            }

            int safePage = Math.max(0, page);
            int safePageSize = Math.max(1, Math.min(pageSize, 500)); // cap page size
            query.setFirstResult(safePage * safePageSize);
            query.setMaxResults(safePageSize);

            return query.list();
        }
    }

    /** Count complaints with the same filters (for pagination) */
    public long count(
            Complaint.Status status,
            String type,
            Long storeId,
            Long customerId,
            Long orderId,
            LocalDateTime from,
            LocalDateTime to,
            String q
    ) {
        StringBuilder hql = new StringBuilder("select count(c.id) from Complaint c where 1=1");
        List<Param> params = new ArrayList<>();

        if (status != null) { hql.append(" and c.status = :status"); params.add(new Param("status", status)); }
        if (type != null && !type.isBlank()) { hql.append(" and c.type = :type"); params.add(new Param("type", type)); }
        if (storeId != null) { hql.append(" and c.storeId = :storeId"); params.add(new Param("storeId", storeId)); }
        if (customerId != null) { hql.append(" and c.customerId = :customerId"); params.add(new Param("customerId", customerId)); }
        if (orderId != null) { hql.append(" and c.orderId = :orderId"); params.add(new Param("orderId", orderId)); }
        if (from != null && to != null) {
            hql.append(" and c.createdAt between :from and :to");
            params.add(new Param("from", from)); params.add(new Param("to", to));
        } else if (from != null) {
            hql.append(" and c.createdAt >= :from"); params.add(new Param("from", from));
        } else if (to != null) {
            hql.append(" and c.createdAt <= :to"); params.add(new Param("to", to));
        }
        if (q != null && !q.isBlank()) {
            hql.append(" and lower(c.text) like :q");
            params.add(new Param("q", "%" + q.toLowerCase() + "%"));
        }

        try (Session session = HbBoot.sf().openSession()) {
            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            for (Param p : params) {
                query.setParameter(p.name, p.value);
            }
            return query.uniqueResult();
        }
    }

    private record Param(String name, Object value) {}
}
