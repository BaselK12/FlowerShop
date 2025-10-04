package il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog;

import java.io.Serializable;

public class CategoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String displayName;
    private String description; // optional, in case you display details later

    public CategoryDTO() {}

    public CategoryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public CategoryDTO(Long id, String name,String displayName, String description) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {return displayName;}
    public void setDisplayName(String displayName) {this.displayName = displayName;}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name;
    }
}
