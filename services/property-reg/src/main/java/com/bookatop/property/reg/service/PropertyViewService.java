package com.bookatop.property.reg.service;

import com.bookatop.catalog.book.api.enums.CategoryTypes;
import com.bookatop.catalog.book.api.model.*;
import com.bookatop.property.reg.api.enums.PropertyJsonType;
import com.bookatop.property.reg.api.model.PropertyView;
import com.bookatop.property.reg.api.model.properties.general.address.PropertyAddress;
import com.bookatop.property.reg.api.model.properties.general.layout.PropertyArea;
import com.bookatop.property.reg.api.model.properties.hotel.HotelProperty;
import com.bookatop.property.reg.clients.CachedCatalogBookClient;
import com.bookatop.property.reg.entity.PropertyEntity;
import com.bookatop.property.reg.exception.PropertyRegException;
import com.bookatop.property.reg.lookups.PropertyLookup;
import com.bookatop.property.reg.repository.PropertyRegRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
public class PropertyViewService {

    private static final byte COUNTRY_INDEX = 0;

    private static final byte CITY_INDEX = 1;

    private static final String PROPERTY_IS_ABSENT = "Property is absent";

    private static final String UNSUPPORTED_JSON_TYPE = "Unsupported JSON type";

    private final PropertyRegRepository propertyRegRepository;

    private final CachedCatalogBookClient cachedCatalogBookClient;

    private final ObjectMapper objectMapper;

    public PropertyViewService(PropertyRegRepository propertyRegRepository,
                               CachedCatalogBookClient cachedCatalogBookClient,
                               ObjectMapper objectMapper) {
        this.propertyRegRepository = propertyRegRepository;
        this.cachedCatalogBookClient = cachedCatalogBookClient;
        this.objectMapper = objectMapper;
    }

    private static void validateSupportedPropertyJsonType(PropertyJsonType propertyJsonType) {
        if (propertyJsonType != PropertyJsonType.HOTEL_JSON)
            throw new PropertyRegException(UNSUPPORTED_JSON_TYPE);
    }

    private BiConsumer<Consumer<String>, Consumer<String>> lookupLayoutArea(Long roomTypeId, Long roomNameId,
                                                                            Long refCatId, String lang) {

        Optional<RoomType> roomType = cachedCatalogBookClient.getPropertyRoomTypes(
                        refCatId,
                        lang).stream()
                .filter(r -> r.getId().equals(roomTypeId))
                .findFirst();

        Optional<RoomName> roomName = roomType.flatMap(r ->
                r.getRoomNames().stream()
                        .filter(n -> n.getId().equals(roomNameId))
                        .findFirst());

        String rtName = roomType.flatMap(r -> Optional.of(r.getTsName())).orElse(null);
        String rName = roomName.flatMap(r -> Optional.of(r.getTsName())).orElse(null);

        return (a, b) -> {
            a.accept(rtName);
            b.accept(rName);
        };
    }

    private PropertyView makePropertyView(PropertyEntity propEntity, String lang) {

        validateSupportedPropertyJsonType(propEntity.getPropertyJsonType());

        try {
            HotelProperty hotelProperty = objectMapper.readValue(propEntity.getPropertyJson(), HotelProperty.class);

            PropertyArea propArea = hotelProperty.getHotelLayoutData().getLayoutArea();

            PropertyLookup.updatePropertyLookup(
                    lookupLayoutArea(
                            propArea.getRoomType().getId(),
                            propArea.getRoomName().getId(),
                            propEntity.getUserPropertyEntity().getRefPropCatId(),
                            lang),
                    (BiConsumer<Consumer<String>, Consumer<String>> f) -> f.accept(
                            propArea.getRoomType()::setName,
                            propArea.getRoomName()::setName)
            );

            PropertyAddress propAddress = hotelProperty.getHotelAboutData().getAddress();

            PropertyLookup.updatePropertyLookup(
                    PropertyLookup.lookupCountry(
                            propAddress.getCountry().getId(),
                            lang,
                            cachedCatalogBookClient),
                    propAddress::setCountry);

            PropertyLookup.updatePropertyLookup(
                    PropertyLookup.lookupCity(
                            propAddress.getCountry().getId(),
                            propAddress.getCity().getId(),
                            lang,
                            cachedCatalogBookClient),
                    propAddress::setCity);

            return new PropertyView(
                    propEntity.getId(),
                    hotelProperty,
                    propEntity.getPropertyJsonType(),
                    propEntity.getUserPropertyEntity().getRefPropCatId());

        } catch (JsonProcessingException e) {
            throw new PropertyRegException(e.getMessage());
        }
    }

    private boolean equalsPropCategoryType(CategoryTypes propCatType, CategoryTypes pCatType) {
        return propCatType.equals(Optional.ofNullable(pCatType).orElse(propCatType));
    }

    private PropertyView queryPropertyViewCountry(PropertyEntity propEntity, String lang, Long countryId,
                                                  CategoryTypes pCatType) {
        try {
            if (Objects.isNull(countryId)) return null;

            HotelProperty hotelProperty = objectMapper.readValue(propEntity.getPropertyJson(), HotelProperty.class);
            PropertyAddress propAddress = hotelProperty.getHotelAboutData().getAddress();

            // todo get all data in constructor instead of making request each time
            Long refPropCatId = propEntity.getUserPropertyEntity().getRefPropCatId();
            PropertyCategory propertyCategory =
                    cachedCatalogBookClient.getPropertyCategory(refPropCatId);

            if (propAddress.getCountry().getId().equals(countryId) &&
                    equalsPropCategoryType(propertyCategory.getCategoryType(), pCatType))
                return makePropertyView(propEntity, lang);

            return null;

        } catch (JsonProcessingException e) {
            throw new PropertyRegException(e.getMessage());
        }
    }

    private PropertyView queryPropertyViewCountryCity(PropertyEntity propEntity, String lang, Long countryId,
                                                      Long cityId, CategoryTypes pCatType) {
        try {

            if (Objects.isNull(countryId) && Objects.isNull(cityId)) return null;

            HotelProperty hotelProperty = objectMapper.readValue(propEntity.getPropertyJson(), HotelProperty.class);
            PropertyAddress propAddress = hotelProperty.getHotelAboutData().getAddress();

            // todo get all data in constructor instead of making request each time
            PropertyCategory propertyCategory =
                    cachedCatalogBookClient.getPropertyCategory(propEntity.getUserPropertyEntity().getRefPropCatId());

            if (propAddress.getCountry().getId().equals(countryId) &&
                    propAddress.getCity().getId().equals(cityId) &&
                    equalsPropCategoryType(propertyCategory.getCategoryType(), pCatType))
                return makePropertyView(propEntity, lang);

            return null;

        } catch (JsonProcessingException e) {
            throw new PropertyRegException(e.getMessage());
        }
    }

    private Long findCountryId(String name, String lang) {
        if (Strings.isNotBlank(name)) {
            Optional<Country> fCountry = cachedCatalogBookClient.getAllCountries(lang).stream()
                    .filter(c -> c.getTsName().equalsIgnoreCase(name)).findFirst();
            return fCountry.flatMap(c -> Optional.of(c.getId())).orElse(null);
        }
        return null;
    }

    private Long findCityId(Long countryId, String name, String lang) {
        if (Objects.nonNull(countryId) && Strings.isNotBlank(name)) {
            Optional<City> fCity = cachedCatalogBookClient.getAllCities(countryId, lang).stream()
                    .filter(c -> c.getTsName().equalsIgnoreCase(name)).findFirst();
            return fCity.flatMap(c -> Optional.of(c.getId())).orElse(null);
        }
        return null;
    }

    public PropertyView getViewProperty(long propId, String lang) {
        PropertyEntity propEntity = propertyRegRepository.findById(propId)
                .orElseThrow(() -> new PropertyRegException(PROPERTY_IS_ABSENT));
        return makePropertyView(propEntity, lang);
    }

    public List<PropertyView> getViewProperties(String lang) {
        return propertyRegRepository.findAllByOrderByIdDesc().stream()
                .map(propEntity -> makePropertyView(propEntity, lang))
                .toList();
    }

    public List<PropertyView> queryViewProperties(String lang, String cc, CategoryTypes pCatType) {
        // "\\,", -1 means to include an empty string to the array result

        Optional<List<String>> allOpts = Optional.ofNullable(cc)
                .flatMap(o -> Optional.of(Arrays.stream(o.split("\\,", -1))
                        .map(String::trim)
                        .toList()));

        return allOpts.flatMap(o -> {

            String countryName = o.get(COUNTRY_INDEX);
            String cityName = o.size() > 1 ? o.get(CITY_INDEX) : null;

            /*
                lang is null means relying on default language (english)
                ?cc=Belarus,Minsk    -> ["Belarus","Minsk"]
                ?cc=Belarus,         -> ["Belarus", ""]          (if comma, city is empty)
                ?cc=Belarus          -> ["Belarus"]              (no comma, city is null)

                *lang null means to use default language defined in app config
             */

            Long countryId = findCountryId(countryName, null);

            if (Objects.isNull(countryId))
                return Optional.empty();

            if (Strings.isNotBlank(countryName) && Objects.nonNull(cityName)) {
                Long cityId = findCityId(countryId, cityName, null);

                if (Objects.isNull(cityId))
                    return Optional.empty();

                return Optional.of(propertyRegRepository.findAllByOrderByIdDesc().stream()
                        .flatMap(propEntity -> Stream.of(queryPropertyViewCountryCity(propEntity, lang, countryId, cityId, pCatType)))
                        .filter(Objects::nonNull)
                        .toList());
            }

            return Optional.of(propertyRegRepository.findAllByOrderByIdDesc().stream()
                    .flatMap(propEntity -> Stream.of(queryPropertyViewCountry(propEntity, lang, countryId, pCatType)))
                    .filter(Objects::nonNull)
                    .toList());

        }).orElse(List.of());
    }
}
