package com.bookatop.property.reg.service;

import com.bookatop.property.reg.entity.PropertyEntity;
import com.bookatop.property.reg.entity.UserPropertyEntity;
import com.bookatop.property.reg.exception.PropertyRegException;
import com.bookatop.property.reg.model.PropertyAdd;
import com.bookatop.property.reg.model.UserProperty;
import com.bookatop.property.reg.repository.PropertyRegRepository;
import com.bookatop.property.reg.repository.UserPropertyRepository;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class PropertyRegService {

    private static final String INVALID_PROPERTY_DATA = "Invalid property data";

    private final PropertyRegRepository propertyRegRepository;

    private final UserPropertyRepository userPropertyRepository;

    private final ObjectMapper objectMapper;

    public PropertyRegService(PropertyRegRepository propertyRegRepository,
                              UserPropertyRepository userPropertyRepository,
                              ObjectMapper objectMapper) {
        this.propertyRegRepository = propertyRegRepository;
        this.userPropertyRepository = userPropertyRepository;
        this.objectMapper = objectMapper;
    }

    private void isValidProperty(PropertyAdd request) {
        isValidPropertyData(request);
    }

    private void isValidPropertyData(PropertyAdd request) {
        if (Objects.isNull(request) ||
                Strings.isBlank(request.getPropJson()) ||
                !isValidJson(request.getPropJson()) ||
                !isValidUserPropertyData(request.getUserProperty()))
            throw new PropertyRegException(INVALID_PROPERTY_DATA);
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (JacksonException e) {
            return false;
        }
        return true;
    }

    private boolean isValidUserPropertyData(UserProperty userProperty) {
        return Objects.nonNull(userProperty.getRefPropTypeId()) &&
                Objects.nonNull(userProperty.getRefPropCatId()) &&
                Objects.nonNull(userProperty.getRefUserId());
    }

    @Transactional
    public void addProperty(PropertyAdd property) {

        isValidProperty(property);

        PropertyEntity propEntity = new PropertyEntity(
                property.getPropJson(),
                property.getPropJsonType(),
                true
        );
        PropertyEntity propResEntity = propertyRegRepository.save(propEntity);

        UserProperty userProperty = property.getUserProperty();

        UserPropertyEntity userPropEntity = new UserPropertyEntity(
                propResEntity.getId(),
                userProperty.getRefPropTypeId(),
                userProperty.getRefPropCatId(),
                userProperty.getRefUserId()
        );
        userPropertyRepository.save(userPropEntity);
    }
}