package il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog;

import java.io.Serializable;
import java.util.List;

public class GetCategoriesResponse implements Serializable {
    private List<CategoryDTO> categories;

    public GetCategoriesResponse(List<CategoryDTO> categories) {
        this.categories = categories;
    }

    public List<CategoryDTO> getCategories() { return categories; }
}