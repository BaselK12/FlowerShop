package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Catalog;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record GetCatalogRequestEvent(GetCatalogRequest request, ConnectionToClient client) {}

