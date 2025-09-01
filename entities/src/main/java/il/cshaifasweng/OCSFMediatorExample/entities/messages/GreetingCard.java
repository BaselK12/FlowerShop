package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class GreetingCard implements Serializable {
    private String text;

    public GreetingCard() {}
    public GreetingCard(String text) { this.text = text; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
