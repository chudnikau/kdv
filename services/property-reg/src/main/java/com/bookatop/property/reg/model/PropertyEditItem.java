package com.bookatop.property.reg.model;

import com.bookatop.catalog.book.api.enums.CategoryTypes;
import com.bookatop.catalog.book.api.enums.PropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/*
    The class describes short information about property (hotel e.g.) element in a property own list
 */

@Getter
@Setter
public class PropertyEditItem implements Serializable {

    @Serial
    private static final long serialVersionUID = -59094649679308183L;

    private Long propertyId;

    private String propertyName;

    private PropertyTypes propertyType;

    private CategoryTypes propertyCategory;

    private String propertyImageUrl;

    private Boolean isActive;

    public PropertyEditItem() {
    }

    public PropertyEditItem(Long propertyId,
                            String propertyName,
                            PropertyTypes propertyType,
                            CategoryTypes propertyCategory,
                            String propertyImageUrl,
                            Boolean isActive) {
        this.propertyId = propertyId;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        this.propertyCategory = propertyCategory;
        this.propertyImageUrl = propertyImageUrl;
        this.isActive = isActive;
    }
}
