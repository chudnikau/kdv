package com.bookatop.property.reg.model;

import com.bookatop.catalog.book.api.enums.CategoryTypes;
import com.bookatop.property.reg.api.enums.PropertyJsonType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class PropertyEditView {

    @NotNull
    @NotBlank
    private Object propJson;

    @NotNull
    private PropertyJsonType propJsonType;

    @NotNull
    private Long propCatId;

    @NotNull
    private CategoryTypes propCatType;

    public PropertyEditView() {
        // POJO
    }

    public PropertyEditView(Object propJson, PropertyJsonType propJsonType, Long propCatId, CategoryTypes propCatType) {
        this.propJson = propJson;
        this.propJsonType = propJsonType;
        this.propCatId = propCatId;
        this.propCatType = propCatType;
    }
}
