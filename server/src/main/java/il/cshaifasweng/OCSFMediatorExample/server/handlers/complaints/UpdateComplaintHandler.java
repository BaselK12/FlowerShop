package il.cshaifasweng.OCSFMediatorExample.server.handlers.complaints;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.*;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.UpdateComplaintRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionManager;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.Session;

public class UpdateComplaintHandler {
    public UpdateComplaintHandler(ServerBus bus) {
        bus.subscribe(UpdateComplaintRequestedEvent.class, evt -> {
            UpdateComplaintRequest req = evt.getRequest();

            try {
                Complaint updated = TX.call((Session s) -> {
                    Complaint c = s.get(Complaint.class, req.getComplaintId());
                    if (c == null) throw new IllegalArgumentException("Complaint not found: " + req.getComplaintId());

                    // apply partial updates
                    if (req.getResolution() != null) {
                        c.setResolution(req.getResolution().trim());
                    }
                    if (req.getNewStatus() != null && !req.getNewStatus().isBlank()) {
                        Complaint.Status target = Complaint.Status.valueOf(req.getNewStatus());
                        // naive guardrail for transitions
                        switch (target) {
                            case IN_PROGRESS, RESOLVED, REJECTED -> c.setStatus(target);
                            default -> { /* OPEN should be initial only */ }
                        }
                    }
                    s.merge(c);
                    return c;
                });

                // acknowledge requester
                if (evt.getClient() != null) {
                    bus.publish(new SendToClientEvent(
                            new UpdateComplaintResponse(true, null, updated),
                            evt.getClient()
                    ));
                }

                // broadcast so tables refresh elsewhere
                ComplaintUpdatedBroadcast broadcast = new ComplaintUpdatedBroadcast(updated);
                for (SessionManager.Session session : SessionManager.get().allSessions()) {
                    bus.publish(new SendToClientEvent(broadcast, session.client()));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                bus.publish(new SendToClientEvent(
                        new UpdateComplaintResponse(false, ex.getMessage(), null), evt.getClient()));
            }
        });
    }
}
