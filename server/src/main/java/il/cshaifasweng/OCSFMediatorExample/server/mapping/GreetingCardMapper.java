package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.GreetingCard;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.GreetingCardDTO;

public class GreetingCardMapper {

    public static GreetingCard fromDTO(GreetingCardDTO dto) {
        if (dto == null) return null;

        GreetingCard card = new GreetingCard();
        card.setMessage(dto.getMessage());
        card.setSenderName(dto.getSenderName());
        card.setRecipientName(dto.getRecipientName());
        card.setRecipientPhone(dto.getRecipientPhone());
        return card;
    }

    public static GreetingCardDTO toDTO(GreetingCard entity) {
        if (entity == null) return null;

        GreetingCardDTO dto = new GreetingCardDTO();
        dto.setMessage(entity.getMessage());
        dto.setSenderName(entity.getSenderName());
        dto.setRecipientName(entity.getRecipientName());
        dto.setRecipientPhone(entity.getRecipientPhone());
        return dto;
    }
}
