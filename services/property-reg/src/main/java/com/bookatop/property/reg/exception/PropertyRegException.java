package com.bookatop.property.reg.exception;

import java.io.Serial;

public class PropertyRegException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4432549780466812679L;

    public PropertyRegException(String message) {
        super(message);
    }
}
