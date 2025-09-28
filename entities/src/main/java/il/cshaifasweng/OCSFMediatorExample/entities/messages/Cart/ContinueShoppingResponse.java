package il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart;

import java.io.Serializable;

public class ContinueShoppingResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;

    public ContinueShoppingResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
