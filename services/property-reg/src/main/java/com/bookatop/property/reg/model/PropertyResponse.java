package com.bookatop.property.reg.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class PropertyResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -8122465584602136328L;

    private String message;

    public PropertyResponse() {
    }

    public PropertyResponse(String message) {
        this.message = message;
    }
}
