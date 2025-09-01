package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record EmployeesOpenEditorEvent(EditorMode mode, Long employeeId, ConnectionToClient client) {
    public enum EditorMode { NEW, EDIT }
}