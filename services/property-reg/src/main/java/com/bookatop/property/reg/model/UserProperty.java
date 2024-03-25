package com.bookatop.property.reg.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class UserProperty implements Serializable {

    @Serial
    private static final long serialVersionUID = -2841305523867016108L;

    @NotNull
    private Long refPropTypeId;

    @NotNull
    private Long refPropCatId;

    @NotNull
    private Long refUserId;

    public UserProperty() {
    }

    public UserProperty(Long refPropTypeId, Long refPropCatId, Long refUserId) {
        this.refPropTypeId = refPropTypeId;
        this.refPropCatId = refPropCatId;
        this.refUserId = refUserId;
    }
}
