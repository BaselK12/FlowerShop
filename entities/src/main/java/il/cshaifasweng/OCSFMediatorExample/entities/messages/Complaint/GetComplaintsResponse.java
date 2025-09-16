package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class GetComplaintsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Complaint> complaints;

    public GetComplaintsResponse(List<Complaint> complaints) {
        this.complaints = complaints == null ? List.of() : List.copyOf(complaints);
    }

    public List<Complaint> getComplaints() {
        return complaints == null ? Collections.emptyList() : complaints;
    }

    @Override public String toString() {
        return "GetComplaintsResponse{count=" + getComplaints().size() + "}";
    }
}
