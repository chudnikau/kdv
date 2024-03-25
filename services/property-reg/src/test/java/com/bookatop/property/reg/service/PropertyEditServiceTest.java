package com.bookatop.property.reg.service;

import com.bookatop.catalog.book.api.enums.CategoryTypes;
import com.bookatop.catalog.book.api.model.City;
import com.bookatop.catalog.book.api.model.Country;
import com.bookatop.catalog.book.api.model.PropertyCategory;
import com.bookatop.property.reg.api.enums.PropertyJsonType;
import com.bookatop.property.reg.api.model.properties.hotel.HotelProperty;
import com.bookatop.property.reg.clients.CachedCatalogBookClient;
import com.bookatop.property.reg.entity.PropertyEntity;
import com.bookatop.property.reg.entity.UserPropertyEntity;
import com.bookatop.property.reg.exception.PropertyRegException;
import com.bookatop.property.reg.model.PropertyEditView;
import com.bookatop.property.reg.model.PropertyUpdate;
import com.bookatop.property.reg.repository.PropertyRegRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application.properties")
class PropertyEditServiceTest {

    public static final String DEF_LANG_ABBREVIATION = "en";

    private static final long REF_USER_ID = 3330981L;

    private static final long REF_PROP_CAT_ID = 222354L;

    private static final long REF_PROP_TYPE_ID = 111309L;

    private static final long PROPERTY_ID = 554901L;

    private static final long BELARUS_COUNTRY_ID = 1;

    private static final long BREST_CITY_ID = 4;

    private static final String VALID_PROP_JSON = "{\"hotelAboutData\":{\"address\":{\"country\":{\"id\":1},\"city\":{\"id\":4}}}}";

    private static final String INVALID_PROP_JSON = "{INVALID_JSON}";

    private static final PropertyJsonType VALID_PROP_JSON_TYPE = PropertyJsonType.MOTEL_JSON;

    @MockBean
    private PropertyRegRepository propertyRegRepository;

    @Autowired
    private PropertyEditService propertyEditService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CachedCatalogBookClient cachedCatalogBookClient;

    private Country belarusCountry;

    private City brestCity;

    private PropertyEntity makePropertyEntity(Long id, String propertyJson, PropertyJsonType propertyJsonType,
                                              Boolean isActive, UserPropertyEntity userPropertyEntity) {
        PropertyEntity propertyEntity = new PropertyEntity();
        propertyEntity.setId(id);
        propertyEntity.setPropertyJson(propertyJson);
        propertyEntity.setPropertyJsonType(propertyJsonType);
        propertyEntity.setIsActive(isActive);
        propertyEntity.setUserPropertyEntity(userPropertyEntity);

        return propertyEntity;
    }

    @BeforeEach
    void testBeforeEach() {
        belarusCountry = new Country();
        belarusCountry.setId(BELARUS_COUNTRY_ID);
        belarusCountry.setTsName("Belarus");

        brestCity = new City();
        brestCity.setId(BREST_CITY_ID);
        brestCity.setTsName("Brest");
    }

    @Test
    void testActivateProperty() {
        PropertyEntity propertyEntity = new PropertyEntity();
        propertyEntity.setId(PROPERTY_ID);
        propertyEntity.setPropertyJson(VALID_PROP_JSON);
        propertyEntity.setPropertyJsonType(VALID_PROP_JSON_TYPE);
        propertyEntity.setIsActive(false);

        UserPropertyEntity propUserEntity = new UserPropertyEntity(
                PROPERTY_ID, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);

        propertyEntity.setUserPropertyEntity(propUserEntity);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        propertyEditService.activeProperty(REF_USER_ID, PROPERTY_ID, true);

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);

        verify(propertyRegRepository, times(1)).save(argThat(argument -> {
            assertEquals(true, argument.getIsActive());
            return true;
        }));
    }

    @Test
    void testDeactivateProperty() {
        PropertyEntity propertyEntity = new PropertyEntity();
        propertyEntity.setId(PROPERTY_ID);
        propertyEntity.setPropertyJson(VALID_PROP_JSON);
        propertyEntity.setPropertyJsonType(VALID_PROP_JSON_TYPE);
        propertyEntity.setIsActive(true);

        UserPropertyEntity propUserEntity = new UserPropertyEntity(
                PROPERTY_ID, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);

        propertyEntity.setUserPropertyEntity(propUserEntity);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        propertyEditService.activeProperty(REF_USER_ID, PROPERTY_ID, false);

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);

        verify(propertyRegRepository, times(1)).save(argThat(argument -> {
            assertEquals(false, argument.getIsActive());
            return true;
        }));
    }

    @Test
    void testPropertyAbsentException() {
        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThrows(PropertyRegException.class, () ->
                propertyEditService.activeProperty(REF_USER_ID, PROPERTY_ID, true));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }

    @Test
    void testUnknownPropertyException() {
        PropertyEntity propertyEntity = new PropertyEntity();
        propertyEntity.setId(PROPERTY_ID);
        propertyEntity.setPropertyJson(VALID_PROP_JSON);
        propertyEntity.setPropertyJsonType(VALID_PROP_JSON_TYPE);
        propertyEntity.setIsActive(false);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyRegException.class, () ->
                propertyEditService.activeProperty(REF_USER_ID, PROPERTY_ID, true));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }

    @Test
    void testIncorrectPropertyOwnerException() {
        long CUSTOM_REF_USER_ID = REF_USER_ID + 1;

        PropertyEntity propertyEntity = new PropertyEntity();
        propertyEntity.setId(PROPERTY_ID);
        propertyEntity.setPropertyJson(VALID_PROP_JSON);
        propertyEntity.setPropertyJsonType(VALID_PROP_JSON_TYPE);
        propertyEntity.setIsActive(false);

        UserPropertyEntity propUserEntity = new UserPropertyEntity(
                PROPERTY_ID, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);

        propertyEntity.setUserPropertyEntity(propUserEntity);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyRegException.class, () ->
                propertyEditService.activeProperty(CUSTOM_REF_USER_ID, PROPERTY_ID, true));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }

    @Test
    void testGetEditProperty() throws JsonProcessingException {
        UserPropertyEntity propUserEntity = new UserPropertyEntity(
                PROPERTY_ID, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);

        PropertyEntity propertyEntity = makePropertyEntity(
                PROPERTY_ID, VALID_PROP_JSON, PropertyJsonType.HOTEL_JSON, true, propUserEntity);

        PropertyCategory propertyCategory = new PropertyCategory(REF_PROP_CAT_ID,
                "pc_hotel_name", "pc_hotel_description", CategoryTypes.HOTEL, true);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));
        when(cachedCatalogBookClient.getPropertyCategory(REF_PROP_CAT_ID)).thenReturn(propertyCategory);
        when(cachedCatalogBookClient.getAllCountries(DEF_LANG_ABBREVIATION)).thenReturn(List.of(belarusCountry));
        when(cachedCatalogBookClient.getAllCities(any(), any())).thenReturn(List.of(brestCity));

        PropertyEditView propertyEditView = propertyEditService.getEditProperty(REF_USER_ID, PROPERTY_ID, DEF_LANG_ABBREVIATION);

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
        verify(cachedCatalogBookClient, times(1)).getPropertyCategory(REF_PROP_CAT_ID);
        verify(cachedCatalogBookClient, times(1)).getAllCountries(any());
        verify(cachedCatalogBookClient, times(1)).getAllCities(any(), any());

        String expectedJson = "{\"hotelAboutData\":{\"address\":{\"country\":{\"id\":1,\"name\":\"Belarus\"},\"city\":{\"id\":4,\"name\":\"Brest\"}}}}";
        HotelProperty expectedPropJson = objectMapper.readValue(expectedJson, HotelProperty.class);

        assertEquals(expectedPropJson, propertyEditView.getPropJson());
        assertEquals(PropertyJsonType.HOTEL_JSON, propertyEditView.getPropJsonType());
    }

    @Test
    void testGetNotOwnedEditProperty() {
        long fakeRefUserId = REF_USER_ID + 1000;
        UserPropertyEntity propUserEntity = new UserPropertyEntity(
                PROPERTY_ID, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, fakeRefUserId);

        PropertyEntity propertyEntity = makePropertyEntity(
                PROPERTY_ID, VALID_PROP_JSON, VALID_PROP_JSON_TYPE, true, propUserEntity);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyRegException.class, () ->
                propertyEditService.getEditProperty(REF_USER_ID, PROPERTY_ID, DEF_LANG_ABBREVIATION));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }

    @Test
    void testGetNotExistEditProperty() {
        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThrows(PropertyRegException.class, () ->
                propertyEditService.getEditProperty(REF_USER_ID, PROPERTY_ID, DEF_LANG_ABBREVIATION));
    }

    @Test
    void testGetNotExistUserEditProperty() {
        PropertyEntity propertyEntity = makePropertyEntity(
                PROPERTY_ID, VALID_PROP_JSON, VALID_PROP_JSON_TYPE, true, null);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyRegException.class, () ->
                propertyEditService.getEditProperty(REF_USER_ID, PROPERTY_ID, DEF_LANG_ABBREVIATION));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }

    @Test
    void testUpdateProperty() {
        String JSON = "{\"value\":\"100\"}";

        PropertyUpdate propertyUpdate =
                new PropertyUpdate(JSON, PropertyJsonType.HOTEL_JSON, REF_PROP_CAT_ID, CategoryTypes.HOTEL);

        UserPropertyEntity propUserEntity = new UserPropertyEntity(
                PROPERTY_ID, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);

        PropertyEntity propertyEntity = makePropertyEntity(
                PROPERTY_ID, VALID_PROP_JSON, PropertyJsonType.HOTEL_JSON, true, propUserEntity);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        propertyEditService.updateProperty(REF_USER_ID, PROPERTY_ID, propertyUpdate);

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);

        verify(propertyRegRepository, times(1)).save(
                argThat(entity -> {
                    assertEquals(JSON, entity.getPropertyJson());
                    return true;
                })
        );
    }

    @Test
    void testUpdateNotOwnedProperty() {
        String JSON = "{\"value\":\"200\"}";

        PropertyUpdate propertyUpdate =
                new PropertyUpdate(JSON, VALID_PROP_JSON_TYPE, REF_PROP_CAT_ID, CategoryTypes.HOTEL);

        long fakeRefUserId = REF_USER_ID + 1000;
        UserPropertyEntity propUserEntity = new UserPropertyEntity(
                PROPERTY_ID, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, fakeRefUserId);

        PropertyEntity propertyEntity = makePropertyEntity(
                PROPERTY_ID, VALID_PROP_JSON, VALID_PROP_JSON_TYPE, true, propUserEntity);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyRegException.class, () ->
                propertyEditService.updateProperty(REF_USER_ID, PROPERTY_ID, propertyUpdate));

        verify(propertyRegRepository, times(1))
                .findById(PROPERTY_ID);
    }

    @Test
    void testUpdateNotExistProperty() {
        PropertyUpdate propertyUpdate =
                new PropertyUpdate(VALID_PROP_JSON, VALID_PROP_JSON_TYPE, REF_PROP_CAT_ID, CategoryTypes.HOTEL);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThrows(PropertyRegException.class, () ->
                propertyEditService.updateProperty(REF_USER_ID, PROPERTY_ID, propertyUpdate));
    }

    @Test
    void testUpdateNullProperty() {
        UserPropertyEntity propUserEntity = new UserPropertyEntity(
                PROPERTY_ID, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);

        PropertyEntity propertyEntity = makePropertyEntity(
                PROPERTY_ID, VALID_PROP_JSON, VALID_PROP_JSON_TYPE, true, propUserEntity);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyRegException.class, () ->
                propertyEditService.updateProperty(REF_USER_ID, PROPERTY_ID, null));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }

    @Test
    void testUnsupportedJsonType() {
        UserPropertyEntity propUserEntity = new UserPropertyEntity(
                PROPERTY_ID, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);

        PropertyEntity propertyEntity = makePropertyEntity(
                PROPERTY_ID, VALID_PROP_JSON, PropertyJsonType.HOSTEL_JSON, true, propUserEntity);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        /* Expected PropertyJsonType.HOTEL_JSON type */
        assertThrows(PropertyRegException.class, () -> propertyEditService.getEditProperty(REF_USER_ID, PROPERTY_ID, DEF_LANG_ABBREVIATION));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }

    @Test
    void testUnsupportedJsonValue() {
        UserPropertyEntity propUserEntity = new UserPropertyEntity(
                PROPERTY_ID, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);

        PropertyEntity propertyEntity = makePropertyEntity(
                PROPERTY_ID, INVALID_PROP_JSON, PropertyJsonType.HOTEL_JSON, true, propUserEntity);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyRegException.class, () -> propertyEditService.getEditProperty(REF_USER_ID, PROPERTY_ID, DEF_LANG_ABBREVIATION));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }
}
