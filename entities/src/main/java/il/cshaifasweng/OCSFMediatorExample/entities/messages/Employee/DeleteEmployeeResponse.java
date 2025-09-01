// DeleteEmployeeResponse.java
package il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee;

import java.io.Serializable;

public class DeleteEmployeeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;

    public DeleteEmployeeResponse() {}
    public DeleteEmployeeResponse(boolean success, String message) {
        this.success = success; this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
