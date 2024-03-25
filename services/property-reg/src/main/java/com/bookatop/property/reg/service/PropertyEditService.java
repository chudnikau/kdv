package com.bookatop.property.reg.service;

import com.bookatop.catalog.book.api.model.PropertyCategory;
import com.bookatop.catalog.book.api.model.PropertyType;
import com.bookatop.property.reg.api.enums.PropertyJsonType;
import com.bookatop.property.reg.api.model.properties.general.address.PropertyAddress;
import com.bookatop.property.reg.api.model.properties.hotel.HotelProperty;
import com.bookatop.property.reg.clients.CachedCatalogBookClient;
import com.bookatop.property.reg.entity.PropertyEntity;
import com.bookatop.property.reg.entity.UserPropertyEntity;
import com.bookatop.property.reg.exception.PropertyRegException;
import com.bookatop.property.reg.lookups.PropertyLookup;
import com.bookatop.property.reg.model.PropertyEditView;
import com.bookatop.property.reg.model.PropertyEditItem;
import com.bookatop.property.reg.model.PropertyUpdate;
import com.bookatop.property.reg.repository.PropertyRegRepository;
import com.bookatop.property.reg.repository.UserPropertyRepository;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PropertyEditService {

    private static final String PROPERTY_IS_ABSENT = "Property is absent";

    private static final String UNKNOWN_PROPERTY_OWNER = "Unknown property owner";

    private static final String INCORRECT_PROPERTY_OWNER = "Incorrect property owner";

    private static final String UNSUPPORTED_JSON_TYPE = "Unsupported JSON type";

    private static final String INVALID_PROPERTY_UPDATE = "Invalid property update data";

    private static final String UNSUPPORTED_PROPERTY_CATEGORY = "Unsupported property category";

    private static final String UNSUPPORTED_PROPERTY_TYPE = "Unsupported property type";

    private final PropertyRegRepository propertyRegRepository;

    private final CachedCatalogBookClient cachedCatalogBookClient;

    private final ObjectMapper objectMapper;

    private final UserPropertyRepository userPropertyRepository;

    public PropertyEditService(PropertyRegRepository propertyRegRepository,
                               CachedCatalogBookClient cachedCatalogBookClient,
                               ObjectMapper objectMapper,
                               UserPropertyRepository userPropertyRepository) {
        this.propertyRegRepository = propertyRegRepository;
        this.cachedCatalogBookClient = cachedCatalogBookClient;
        this.objectMapper = objectMapper;
        this.userPropertyRepository = userPropertyRepository;
    }

    private PropertyEntity requestPropertyEntity(long userId, long propId) {
        PropertyEntity propertyEntity = propertyRegRepository.findById(propId)
                .orElseThrow(() -> new PropertyRegException(PROPERTY_IS_ABSENT));

        UserPropertyEntity userPropertyEntity = Optional.ofNullable(propertyEntity.getUserPropertyEntity())
                .orElseThrow(() -> new PropertyRegException(UNKNOWN_PROPERTY_OWNER));

        if (!userPropertyEntity.getRefUserId().equals(userId))
            throw new PropertyRegException(INCORRECT_PROPERTY_OWNER);

        return propertyEntity;
    }

    private PropertyCategory findPropertyCategory(long catId) {
        Optional<PropertyCategory> propCategory =
                Optional.of(cachedCatalogBookClient.getPropertyCategory(catId));

        return propCategory.orElseThrow(() -> new PropertyRegException(UNSUPPORTED_PROPERTY_CATEGORY));
    }

    private static void validateSupportedPropertyJsonType(PropertyJsonType propertyJsonType) {
        if (propertyJsonType != PropertyJsonType.HOTEL_JSON)
            throw new PropertyRegException(UNSUPPORTED_JSON_TYPE);
    }

    @Transactional
    public void activeProperty(long userId, long propId, Boolean activate) {
        PropertyEntity propertyEntity = requestPropertyEntity(userId, propId);

        propertyEntity.setIsActive(activate);

        propertyRegRepository.save(propertyEntity);
    }

    public PropertyEditView getEditProperty(long userId, long propId, String lang) {

        PropertyEntity propEntity = requestPropertyEntity(userId, propId);

        validateSupportedPropertyJsonType(propEntity.getPropertyJsonType());

        try {
            HotelProperty hotelProperty = objectMapper.readValue(propEntity.getPropertyJson(), HotelProperty.class);

            PropertyAddress propAddress = hotelProperty.getHotelAboutData().getAddress();

            Long countryId = propAddress.getCountry().getId();
            Long cityId = propAddress.getCity().getId();

            PropertyLookup.updatePropertyLookup(
                    PropertyLookup.lookupCountry(countryId, lang, cachedCatalogBookClient),
                    propAddress::setCountry);

            PropertyLookup.updatePropertyLookup(
                    PropertyLookup.lookupCity(countryId, cityId, lang, cachedCatalogBookClient),
                    propAddress::setCity);

            PropertyCategory propCategory =
                    findPropertyCategory(propEntity.getUserPropertyEntity().getRefPropCatId());

            return new PropertyEditView(hotelProperty,
                    propEntity.getPropertyJsonType(),
                    propCategory.getId(),
                    propCategory.getCategoryType());

        } catch (IOException e) {
            throw new PropertyRegException(e.getMessage());
        }
    }

    @Transactional
    public void updateProperty(long userId, long propId, PropertyUpdate propUpdate) {
        PropertyEntity propertyEntity = requestPropertyEntity(userId, propId);

        PropertyUpdate pUpdate = Optional.ofNullable(propUpdate)
                .orElseThrow(() -> new PropertyRegException(INVALID_PROPERTY_UPDATE));

        validateSupportedPropertyJsonType(pUpdate.getPropJsonType());

        propertyEntity.setPropertyJson(pUpdate.getPropJson());

        propertyRegRepository.save(propertyEntity);
    }

    private String readPropertyName(String json, PropertyJsonType jsonType) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            return Optional.ofNullable(rootNode.get("hotelAboutData"))
                    .flatMap(rootElement -> Optional.ofNullable(rootElement.get("propName")))
                    .flatMap(value -> Optional.ofNullable(value.asText())).orElse(null);
        } catch (JacksonException e) {
            return null;
        }
    }

    private String readImageUrl(String json, PropertyJsonType jsonType) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode images = Optional.ofNullable(rootNode.get("hotelPhotosData")).flatMap(rootElement ->
                    Optional.ofNullable(rootElement.get("images"))).orElse(null);
            if (Objects.nonNull(images) && images.isArray()) {
                Iterator<JsonNode> itr = images.iterator();
                if (itr.hasNext()) {
                    JsonNode url = itr.next();
                    return url.get("url").asText();
                }
            }
            return null;
        } catch (JacksonException e) {
            return null;
        }
    }

    private PropertyType findPropertyType(long typeId) {
        Optional<PropertyType> pType =
                cachedCatalogBookClient.getPropertyTypes().stream()
                        .filter(p -> p.getId().equals(typeId)).findFirst();

        return pType.orElseThrow(() -> new PropertyRegException(UNSUPPORTED_PROPERTY_TYPE));
    }

    public List<PropertyEditItem> getEditPropertyList(Long userId, String lang) {
        return userPropertyRepository.findAllByRefUserIdOrderByPropertyIdDesc(userId).stream().map(p -> {
                    String json = p.getPropertyEntity().getPropertyJson();
                    PropertyJsonType jsonType = p.getPropertyEntity().getPropertyJsonType();

                    return new PropertyEditItem(
                            p.getPropertyId(),
                            readPropertyName(json, jsonType),
                            findPropertyType(p.getRefPropTypeId()).getPropertyType(),
                            findPropertyCategory(p.getRefPropCatId()).getCategoryType(),
                            readImageUrl(json, jsonType),
                            p.getPropertyEntity().getIsActive()
                    );
                }
        ).toList();
    }
}
