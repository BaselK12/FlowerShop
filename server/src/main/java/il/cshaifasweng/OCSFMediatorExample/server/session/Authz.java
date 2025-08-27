package il.cshaifasweng.OCSFMediatorExample.server.session;

import il.cshaifasweng.OCSFMediatorExample.server.session.SessionManager;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import java.util.Arrays;

public final class Authz {
    private Authz() {}

    public static void requireRole(SessionManager sessions,
                                   ConnectionToClient client,
                                   String... roles) throws SecurityException {
        var session = sessions.getByClient(client)
                .orElseThrow(() -> new SecurityException("Not logged in"));
        var ok = Arrays.stream(roles)
                .anyMatch(r -> r.equalsIgnoreCase(session.role()));
        if (!ok) throw new SecurityException("Insufficient role");
    }
}
