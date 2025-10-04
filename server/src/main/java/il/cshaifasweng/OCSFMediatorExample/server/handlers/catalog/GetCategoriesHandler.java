package il.cshaifasweng.OCSFMediatorExample.server.handlers.catalog;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Category;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.CategoryDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCategoriesResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Catalog.GetCategoriesRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.util.List;

public class GetCategoriesHandler {
    public GetCategoriesHandler(ServerBus bus) {
        bus.subscribe(GetCategoriesRequestEvent.class, evt -> {
            try {
                List<Category> results = TX.call(s ->
                        s.createQuery("from Category", Category.class).list()
                );

                List<CategoryDTO> payload = results.stream()
                        .map(c -> new CategoryDTO(c.getId(), c.getName(),c.getDisplayName(), c.getDescription()))
                        .toList();

                bus.publish(new SendToClientEvent(new GetCategoriesResponse(payload), evt.client()));
            } catch (Exception e) {
                e.printStackTrace();
                bus.publish(new SendToClientEvent(new GetCategoriesResponse(List.of()), evt.client()));
            }
        });
    }
}