package com.bookatop.property.reg.handler;

import com.bookatop.property.reg.exception.ImageStorageServiceException;
import com.bookatop.property.reg.exception.PropertyRegException;
import com.bookatop.property.reg.model.ErrorResponse;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GlobalPropertyRegExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalPropertyRegExceptionHandler.class);

    @ExceptionHandler(value = {PropertyRegException.class})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public @ResponseBody ErrorResponse handlePropertyRegException(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), ex.getMessage());
    }

    @ExceptionHandler(value = ImageStorageServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleImageStorageServiceException(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), ex.getMessage());
    }

    @ExceptionHandler(value = {PSQLException.class, IOException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handlePassDataException(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return new ErrorResponse(HttpStatus.BAD_GATEWAY.value(), ex.getMessage());
    }
}
