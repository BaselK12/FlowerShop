package il.cshaifasweng.OCSFMediatorExample.server.handlers.complaints;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Ack;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.ComplaintDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.GetComplaintsResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.ComplaintsFetchRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.CustomerLoginNavEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.ComplaintMapper;

import java.util.List;
import java.util.stream.Collectors;


public class GetComplaintsHandler {

    private final ComplaintRepository repo = new ComplaintRepository();

    public GetComplaintsHandler(ServerBus bus) {
        bus.subscribe(ComplaintsFetchRequestedEvent.class, evt -> {
            try {
                // Fetch complaints (no pagination in your current response)
                List<Complaint> complaints = repo.find(
                        evt.getStatus() != null ? Complaint.Status.valueOf(evt.getStatus()) : null,
                        evt.getType(),
                        evt.getStoreId(),
                        evt.getCustomerId(),
                        evt.getOrderId(),
                        evt.getFrom(),
                        evt.getTo(),
                        evt.getQ(),
                        evt.getSortBy(),
                        evt.isSortDesc(),
                        evt.getPage(),
                        evt.getPageSize()
                );

                // Send back as GetComplaintsResponse
                bus.publish(new SendToClientEvent(
                        new GetComplaintsResponse(complaints),
                        evt.getClient()
                ));

            } catch (Exception e) {
                e.printStackTrace();
                bus.publish(new SendToClientEvent(
                        new RuntimeException("Failed to fetch complaints: " + e.getMessage()),
                        evt.getClient()
                ));
            }
        });
    }

}