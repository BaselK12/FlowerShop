package il.cshaifasweng.OCSFMediatorExample.server.handlers.complaints;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint.Status;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SubmitComplaintRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.HbBoot;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;

public class SubmitComplaintHandler {

    public void handle(SubmitComplaintRequestEvent event) {
        SubmitComplaintRequest request = event.getRequest();
        ConnectionToClient client = event.getClient();

        // Convert request → Complaint entity
        Complaint complaint = new Complaint();
        complaint.setCustomerId(request.getCustomerId());
        complaint.setOrderId(request.getOrderId());
        complaint.setType(request.getCategory());          // mapped to 'type' in Complaint
        complaint.setSubject(request.getSubject());
        complaint.setText(request.getMessage());          // mapped to 'text' in Complaint
        complaint.setAnonymous(request.isAnonymous());
        complaint.setEmail(request.getEmail());
        complaint.setPhone(request.getPhone());

        // Initialize default fields
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setStatus(Status.OPEN);

        try (Session session = HbBoot.sf().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(complaint);
            tx.commit();

            // Send success response (convert ID to String)
            SubmitComplaintResponse resp = new SubmitComplaintResponse(
                    true,
                    null,
                    complaint.getId() != null ? complaint.getId().toString() : null
            );
            client.sendToClient(resp);

        } catch (Exception e) {
            e.printStackTrace();

            // Send failure response
            SubmitComplaintResponse resp = new SubmitComplaintResponse(
                    false,
                    e.getMessage(),
                    null
            );
            try {
                client.sendToClient(resp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
