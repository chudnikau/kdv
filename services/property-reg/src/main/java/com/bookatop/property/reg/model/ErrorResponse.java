package com.bookatop.property.reg.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class ErrorResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 7760150557159293786L;

    private int status;

    private String message;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
