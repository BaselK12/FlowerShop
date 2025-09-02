package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import java.util.List;

public class Catalog implements Serializable {
    private List<Flower> flowers;
    private List<GreetingCard> greetingCards;

    public Catalog() {}

    public Catalog(List<Flower> flowers, List<GreetingCard> greetingCards) {
        this.flowers = flowers;
        this.greetingCards = greetingCards;
    }

    public List<Flower> getFlowers() { return flowers; }
    public void setFlowers(List<Flower> flowers) { this.flowers = flowers; }

    public List<GreetingCard> getGreetingCards() { return greetingCards; }
    public void setGreetingCards(List<GreetingCard> greetingCards) { this.greetingCards = greetingCards; }
}
