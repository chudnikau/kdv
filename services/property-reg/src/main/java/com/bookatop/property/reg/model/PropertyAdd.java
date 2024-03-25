package com.bookatop.property.reg.model;

import com.bookatop.property.reg.api.enums.PropertyJsonType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class PropertyAdd implements Serializable {

    @Serial
    private static final long serialVersionUID = 8735058873103131523L;

    @NotNull
    @NotBlank
    private String propJson;

    @NotNull
    private PropertyJsonType propJsonType;

    @NotNull
    private UserProperty userProperty;

    public PropertyAdd() {
    }

    public PropertyAdd(String propJson, PropertyJsonType propJsonType, UserProperty userProperty) {
        this.propJson = propJson;
        this.propJsonType = propJsonType;
        this.userProperty = userProperty;
    }
}
