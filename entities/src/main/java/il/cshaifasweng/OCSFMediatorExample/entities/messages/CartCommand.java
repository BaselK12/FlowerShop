package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;
import java.util.List;

public class CartCommand implements Serializable {
    public enum Op { CREATE, ADD, UPDATE, REMOVE }

    private Op op;
    private List<CartItem> items;

    public CartCommand() {}
    public CartCommand(Op op, List<CartItem> items) {
        this.op = op;
        this.items = items;
    }

    public Op getOp() { return op; }
    public void setOp(Op op) { this.op = op; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}
