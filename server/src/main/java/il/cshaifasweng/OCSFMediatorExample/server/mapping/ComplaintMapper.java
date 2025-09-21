package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.ComplaintDTO;

public class ComplaintMapper {

    // Map from DTO → Entity
    public static Complaint toEntity(ComplaintDTO dto) {
        if (dto == null) return null;

        Complaint complaint = new Complaint();

        // Fields coming directly from the DTO
        complaint.setType(dto.getCategory());   // maps "category" → "type"
        complaint.setOrderId(dto.getOrderId());

        // Merge subject + message into one text (your entity only has 'text')
        String text = dto.getSubject() != null && !dto.getSubject().isBlank()
                ? dto.getSubject() + ": " + dto.getMessage()
                : dto.getMessage();
        complaint.setText(text);

        // If anonymous → skip customer identifiers
        if (!dto.isAnonymous()) {
            // Here you’d typically resolve customerId from email/phone in DB.
            // For demo purposes we just embed email/phone into resolution or text.
            // In production: lookup the actual customer entity by email/phone.
            complaint.setCustomerId(dto.getEmail());
        } else {
            complaint.setCustomerId(null);
        }

        // Default status + createdAt for new complaints
        complaint.setStatus(Complaint.Status.OPEN);
        complaint.setCreatedAt(java.time.LocalDateTime.now());

        return complaint;
    }

    // Map from Entity → DTO
    public static ComplaintDTO toDTO(Complaint entity) {
        if (entity == null) return null;

        // Split text into subject + message heuristically
        String subject = null;
        String message = entity.getText();
        if (message != null && message.contains(":")) {
            int idx = message.indexOf(":");
            subject = message.substring(0, idx).trim();
            message = message.substring(idx + 1).trim();
        }

        ComplaintDTO dto = new ComplaintDTO();
        dto.setCategory(entity.getType());
        dto.setOrderId(entity.getOrderId());
        dto.setSubject(subject);
        dto.setMessage(message);

        // If no customerId → treat as anonymous
        boolean anonymous = (entity.getCustomerId() == null || entity.getCustomerId().isBlank());
        dto.setAnonymous(anonymous);

        // Only set contact info if not anonymous
        if (!anonymous) {
            dto.setEmail(entity.getCustomerId()); // assuming stored email
            // phone isn’t stored in Complaint, would be resolved elsewhere
            dto.setPhone(null);
        }

        return dto;
    }
}