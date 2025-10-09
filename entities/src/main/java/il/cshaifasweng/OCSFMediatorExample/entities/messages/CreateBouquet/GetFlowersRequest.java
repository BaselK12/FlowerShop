package il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet;


import java.io.Serializable;

public class GetFlowersRequest implements Serializable {
    private final boolean singlesOnly;
    public GetFlowersRequest(boolean singlesOnly) { this.singlesOnly = singlesOnly; }
    public boolean isSinglesOnly() { return singlesOnly; }
}
