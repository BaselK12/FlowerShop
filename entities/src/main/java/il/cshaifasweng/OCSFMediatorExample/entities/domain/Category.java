package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;

public enum Category implements Serializable {
    BOUQUET("Bouquet"),
    ROMANTIC("Romantic"),
    POT_PLANT("Pot Plant"),
    EXOTIC("Exotic"),
    FUNERAL("Funeral"),
    BIRTHDAY("Birthday");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
