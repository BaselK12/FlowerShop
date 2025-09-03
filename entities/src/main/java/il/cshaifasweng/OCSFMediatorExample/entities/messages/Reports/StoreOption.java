package il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports;

import java.io.Serial;
import java.io.Serializable;

public class StoreOption implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    public String id;
    public String name;

    public StoreOption() {} // no-arg for serialization

    public StoreOption(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String toString() { return name; }
}
