package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;

public class Company implements Serializable {
    private String id;
    private String name;
    private String supportEmail;
    private String supportPhone;

    public Company() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSupportEmail() { return supportEmail; }
    public void setSupportEmail(String supportEmail) { this.supportEmail = supportEmail; }

    public String getSupportPhone() { return supportPhone; }
    public void setSupportPhone(String supportPhone) { this.supportPhone = supportPhone; }
}
