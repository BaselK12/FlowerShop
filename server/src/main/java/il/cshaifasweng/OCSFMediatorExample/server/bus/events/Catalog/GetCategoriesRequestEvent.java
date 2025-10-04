package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Catalog;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCategoriesRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record GetCategoriesRequestEvent(GetCategoriesRequest request, ConnectionToClient client) { }
