package il.cshaifasweng.OCSFMediatorExample.server.handlers.complaints;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SubmitComplaintRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.ComplaintMapper;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.Session;

public class SubmitComplaintHandler {


    private static final long DEFAULT_STORE_ID = 1;

    /** Your App.java does: new SubmitComplaintHandler(bus); */
    public SubmitComplaintHandler(ServerBus bus) {
        register(bus);
        System.out.println("[SRV/SubmitComplaintHandler] subscribed");
    }

    /** Also allow manual registration if you’re into that. */
    public SubmitComplaintHandler() {}

    /** Wire the handler to the bus. */
    public void register(ServerBus bus) {
        bus.subscribe(SubmitComplaintRequestEvent.class, this::handle);
    }

    /** Validate, persist, reply. */
    private void handle(SubmitComplaintRequestEvent evt) {
        final SubmitComplaintRequest req = evt.getRequest();
        final ConnectionToClient client = evt.getClient();

        try {
            String complaintId = TX.call((Session s) -> {
                var entity = ComplaintMapper.fromRequest(s, req, DEFAULT_STORE_ID);
                s.persist(entity);
                s.flush(); // ensure id is generated
                Object key = s.getIdentifier(entity);
                return key == null ? null : String.valueOf(key);
            });

            // DTO ctor: (ok, reason, complaintId)
            safeSend(client, new SubmitComplaintResponse(true, null, complaintId));

        } catch (IllegalArgumentException ex) {
            safeSend(client, new SubmitComplaintResponse(false, ex.getMessage(), null));

        } catch (Exception ex) {
            ex.printStackTrace();
            safeSend(client, new SubmitComplaintResponse(false, "Server error while submitting complaint.", null));
        }
    }

    private void safeSend(ConnectionToClient client, Object msg) {
        try { client.sendToClient(msg); } catch (Exception ignore) {}
    }
}
