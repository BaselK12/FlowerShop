package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.ComplaintDTO;

import java.time.LocalDateTime;

public class ComplaintMapper {

    // DTO → Entity (demo version)
    public static Complaint toEntity(ComplaintDTO dto) {
        if (dto == null) return null;

        Complaint complaint = new Complaint();

        // map type/category
        complaint.setType(dto.getCategory());

        // orderId: convert from String to Long if possible
        if (dto.getOrderId() != null) {
            try {
                complaint.setOrderId(Long.valueOf(dto.getOrderId()));
            } catch (NumberFormatException e) {
                complaint.setOrderId(null); // fallback if not numeric
            }
        }

        // merge subject + message
        String text = dto.getSubject() != null && !dto.getSubject().isBlank()
                ? dto.getSubject() + ": " + dto.getMessage()
                : dto.getMessage();
        complaint.setText(text);

        // demo: we don’t resolve customer from DB, just set null or fake ID
        if (dto.isAnonymous()) {
            complaint.setCustomerId(null);
        } else {
            // put a dummy ID (in real code: lookup customer by email/phone)
            complaint.setCustomerId(1L);
        }

        // set defaults
        complaint.setStatus(Complaint.Status.OPEN);
        complaint.setCreatedAt(LocalDateTime.now());

        return complaint;
    }

    // Entity → DTO (demo version)
    public static ComplaintDTO toDTO(Complaint entity) {
        if (entity == null) return null;

        // split subject + message
        String subject = entity.getSubject();
        String message = entity.getText();
        if (message != null && message.contains(":")) {
            int idx = message.indexOf(":");
            subject = message.substring(0, idx).trim();
            message = message.substring(idx + 1).trim();
        }

        ComplaintDTO dto = new ComplaintDTO();
        dto.setCategory(entity.getType());
        dto.setOrderId(entity.getOrderId() != null ? entity.getOrderId().toString() : null);
        dto.setSubject(subject);
        dto.setMessage(message);

        // anonymous if no customerId
        boolean anonymous = (entity.getCustomerId() == null);
        dto.setAnonymous(anonymous);

        if (!anonymous) {
            // fake email/phone for demo (in real code: fetch from Customer table)
            dto.setEmail("demo@example.com");
            dto.setPhone("000-0000000");
        }

        return dto;
    }
}
