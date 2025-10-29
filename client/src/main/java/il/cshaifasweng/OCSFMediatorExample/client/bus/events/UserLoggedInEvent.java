package il.cshaifasweng.OCSFMediatorExample.client.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;

/** Local UI event: fired after a successful login, so the opener can refresh UI. */
public record UserLoggedInEvent(String username, String displayName, Role role) {}
