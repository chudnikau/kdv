package com.bookatop.property.reg.exception;

import java.io.Serial;

public class ImageStorageServiceException extends PropertyRegException {

    @Serial
    private static final long serialVersionUID = -4774397046680317622L;

    public ImageStorageServiceException(String message) {
        super(message);
    }
}
