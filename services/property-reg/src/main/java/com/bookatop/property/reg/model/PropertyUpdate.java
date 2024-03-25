package com.bookatop.property.reg.model;

import com.bookatop.catalog.book.api.enums.CategoryTypes;
import com.bookatop.property.reg.api.enums.PropertyJsonType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class PropertyUpdate implements Serializable {

    @Serial
    private static final long serialVersionUID = 6164322104631444965L;

    @NotNull
    @NotBlank
    private String propJson;

    @NotNull
    private PropertyJsonType propJsonType;

    @NotNull
    private Long propCatId;

    @NotNull
    private CategoryTypes propCatType;

    public PropertyUpdate() {
    }

    public PropertyUpdate(String propJson, PropertyJsonType propJsonType, Long propCatId,
                          CategoryTypes propCatType) {
        this.propJson = propJson;
        this.propJsonType = propJsonType;
        this.propCatId = propCatId;
        this.propCatType = propCatType;
    }
}
