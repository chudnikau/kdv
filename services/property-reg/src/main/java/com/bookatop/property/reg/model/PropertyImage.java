package com.bookatop.property.reg.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class PropertyImage implements Serializable {

    @Serial
    private static final long serialVersionUID = -5849182215668462993L;

    private String url;

    public PropertyImage() {
    }

    public PropertyImage(String url) {
        this.url = url;
    }
}