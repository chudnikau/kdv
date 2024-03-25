package com.bookatop.property.reg.service;

import com.bookatop.catalog.book.api.enums.CategoryTypes;
import com.bookatop.catalog.book.api.model.*;
import com.bookatop.property.reg.api.enums.PropertyJsonType;
import com.bookatop.property.reg.api.model.PropertyView;
import com.bookatop.property.reg.api.model.properties.hotel.HotelProperty;
import com.bookatop.property.reg.clients.CachedCatalogBookClient;
import com.bookatop.property.reg.entity.PropertyEntity;
import com.bookatop.property.reg.entity.UserPropertyEntity;
import com.bookatop.property.reg.exception.PropertyRegException;
import com.bookatop.property.reg.repository.PropertyRegRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application.properties")
class PropertyViewServiceTest {

    private static final String DEF_LANG_ABBREVIATION = "en";

    private static final long REF_USER_ID = 333098L;

    private static final long REF_PROP_CAT_ID = 222345L;

    private static final long REF_PROP_TYPE_ID = 111345L;

    private static final long PROPERTY_ID = 555896L;

    private static final long BELARUS_COUNTRY_ID = 1;

    private static final long MINSK_CITY_ID = 10;

    private static final long HOTEL_PROPERTY_CATEGORY_ID = 1897;

    private static final String INVALID_PROP_JSON = "{INVALID_JSON}";

    @MockBean
    private PropertyRegRepository propertyRegRepository;

    @MockBean
    private CachedCatalogBookClient cachedCatalogBookClient;

    @Autowired
    private PropertyViewService propertyViewService;

    @Autowired
    private ObjectMapper objectMapper;

    private Country belarusCountry;

    private City minskCity;

    private PropertyCategory hotelPropertyCategory;

    private UserPropertyEntity createUserPropertyEntity() {
        return createUserPropertyEntity(PROPERTY_ID);
    }

    private UserPropertyEntity createUserPropertyEntity(Long propId) {
        return new UserPropertyEntity(propId, REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);
    }

    private PropertyEntity createPropertyEntity(UserPropertyEntity userPropertyEntry) {
        PropertyEntity propertyEntity = new PropertyEntity();

        propertyEntity.setId(PROPERTY_ID);
        propertyEntity.setPropertyJson("{\"hotelAboutData\":{\"address\":{\"country\":{\"id\":1},\"city\":{\"id\":10}}},\"hotelLayoutData\":{\"layoutArea\":{\"roomType\":{\"id\":9},\"roomName\":{\"id\":388}}}}");
        propertyEntity.setPropertyJsonType(PropertyJsonType.HOTEL_JSON);
        propertyEntity.setIsActive(false);

        propertyEntity.setUserPropertyEntity(userPropertyEntry);
        return propertyEntity;
    }

    private PropertyEntity createPropertyEntity(Long propId, Long countryId, Long cityId, UserPropertyEntity userPropertyEntry) {
        PropertyEntity propertyEntity = new PropertyEntity();

        propertyEntity.setId(propId);
        propertyEntity.setPropertyJson("{\"hotelAboutData\":{\"address\":{\"country\":{\"id\":" + countryId +
                "},\"city\":{\"id\":" + cityId +
                "}}},\"hotelLayoutData\":{\"layoutArea\":{\"roomType\":{\"id\":9},\"roomName\":{\"id\":388}}}}");
        propertyEntity.setPropertyJsonType(PropertyJsonType.HOTEL_JSON);
        propertyEntity.setIsActive(false);

        propertyEntity.setUserPropertyEntity(userPropertyEntry);
        return propertyEntity;
    }

    @BeforeEach
    void testBeforeEach() {
        belarusCountry = new Country();
        belarusCountry.setId(BELARUS_COUNTRY_ID);
        belarusCountry.setTsName("Belarus");

        minskCity = new City();
        minskCity.setId(MINSK_CITY_ID);
        minskCity.setTsName("Minsk");

        hotelPropertyCategory = new PropertyCategory();
        hotelPropertyCategory.setId(HOTEL_PROPERTY_CATEGORY_ID);
        hotelPropertyCategory.setTsName("HotelCategoryTsName");
        hotelPropertyCategory.setTsDescription("HotelCategoryTsDescription");
        hotelPropertyCategory.setCategoryType(CategoryTypes.HOTEL);
        hotelPropertyCategory.setIsActive(true);
    }

    @Test
    void testPropertyView() throws JsonProcessingException {
        PropertyEntity propertyEntity = createPropertyEntity(createUserPropertyEntity());

        List<RoomName> roomNames = List.of(new RoomName(388L, "family view on sea"));
        List<RoomType> roomType = List.of(new RoomType(9L, "family", roomNames));

        List<Country> countries = List.of(belarusCountry);
        List<City> cities = List.of(minskCity);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));
        when(cachedCatalogBookClient.getPropertyRoomTypes(REF_PROP_CAT_ID, DEF_LANG_ABBREVIATION)).thenReturn(roomType);
        when(cachedCatalogBookClient.getAllCountries(DEF_LANG_ABBREVIATION)).thenReturn(countries);
        when(cachedCatalogBookClient.getAllCities(BELARUS_COUNTRY_ID, DEF_LANG_ABBREVIATION)).thenReturn(cities);

        PropertyView propertyView = propertyViewService.getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION);

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
        verify(cachedCatalogBookClient, times(1)).getPropertyRoomTypes(REF_PROP_CAT_ID, DEF_LANG_ABBREVIATION);
        verify(cachedCatalogBookClient, times(1)).getAllCountries(DEF_LANG_ABBREVIATION);
        verify(cachedCatalogBookClient, times(1)).getAllCities(BELARUS_COUNTRY_ID, DEF_LANG_ABBREVIATION);

        String expectedJson = "{\"hotelAboutData\":{\"address\":{\"country\":{\"id\":1,\"name\":\"Belarus\"},\"city\":{\"id\":10,\"name\":\"Minsk\"}}},\"hotelLayoutData\":{\"layoutArea\":{\"roomType\":{\"id\":9,\"name\":\"family\"},\"roomName\":{\"id\":388,\"name\":\"family view on sea\"}}}}";

        assertEquals(REF_PROP_CAT_ID, propertyView.getPropCatId());
        assertEquals(PropertyJsonType.HOTEL_JSON, propertyView.getPropJsonType());
        assertEquals(expectedJson, objectMapper.writeValueAsString(propertyView.getPropJson()));
    }

    @Test
    void testPropertyNoFound() {
        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThrows(PropertyRegException.class, () -> propertyViewService.getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }

    @Test
    void testPropertyWithUnsupportedJsonType() {
        PropertyEntity propertyEntity = createPropertyEntity(createUserPropertyEntity());
        propertyEntity.setPropertyJsonType(PropertyJsonType.GUEST_HOUSE_JSON);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyRegException.class, () -> propertyViewService.getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }

    @Test
    void testPropertyWithNoLookups() throws JsonProcessingException {
        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(createPropertyEntity(createUserPropertyEntity())));
        when(cachedCatalogBookClient.getPropertyRoomTypes(REF_PROP_CAT_ID, DEF_LANG_ABBREVIATION)).thenReturn(List.of());
        when(cachedCatalogBookClient.getAllCountries(DEF_LANG_ABBREVIATION)).thenReturn(List.of());
        when(cachedCatalogBookClient.getAllCities(BELARUS_COUNTRY_ID, DEF_LANG_ABBREVIATION)).thenReturn(List.of());

        PropertyView propertyView = propertyViewService.getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION);

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
        verify(cachedCatalogBookClient, times(1)).getPropertyRoomTypes(REF_PROP_CAT_ID, DEF_LANG_ABBREVIATION);
        verify(cachedCatalogBookClient, times(1)).getAllCountries(DEF_LANG_ABBREVIATION);
        verify(cachedCatalogBookClient, times(1)).getAllCities(BELARUS_COUNTRY_ID, DEF_LANG_ABBREVIATION);

        String expectedJson = "{\"hotelAboutData\":{\"address\":{\"country\":{\"id\":1},\"city\":{\"id\":10}}},\"hotelLayoutData\":{\"layoutArea\":{\"roomType\":{\"id\":9},\"roomName\":{\"id\":388}}}}";

        assertEquals(REF_PROP_CAT_ID, propertyView.getPropCatId());
        assertEquals(PropertyJsonType.HOTEL_JSON, propertyView.getPropJsonType());
        assertEquals(expectedJson, objectMapper.writeValueAsString(propertyView.getPropJson()));
    }

    @Test
    void testPropertyWithInvalidJson() {

        PropertyEntity propertyEntity = createPropertyEntity(createUserPropertyEntity());
        propertyEntity.setPropertyJson(INVALID_PROP_JSON);

        when(propertyRegRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyRegException.class, () -> propertyViewService.getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION));

        verify(propertyRegRepository, times(1)).findById(PROPERTY_ID);
    }

    @Test
    void testPropertyListData() throws JsonProcessingException {
        PropertyEntity propertyEntity = createPropertyEntity(createUserPropertyEntity());

        List<RoomName> roomNames = List.of(new RoomName(388L, "family view on sea"));
        List<RoomType> roomType = List.of(new RoomType(9L, "family", roomNames));

        Country belarusCountry = new Country();
        belarusCountry.setId(BELARUS_COUNTRY_ID);
        belarusCountry.setTsName("Belarus");

        List<Country> countries = List.of(belarusCountry);

        City minskCity = new City();
        minskCity.setId(MINSK_CITY_ID);
        minskCity.setTsName("Minsk");

        List<City> cities = List.of(minskCity);

        when(propertyRegRepository.findAllByOrderByIdDesc()).thenReturn(List.of(propertyEntity));

        when(cachedCatalogBookClient.getPropertyRoomTypes(REF_PROP_CAT_ID, DEF_LANG_ABBREVIATION)).thenReturn(roomType);
        when(cachedCatalogBookClient.getAllCountries(DEF_LANG_ABBREVIATION)).thenReturn(countries);
        when(cachedCatalogBookClient.getAllCities(BELARUS_COUNTRY_ID, DEF_LANG_ABBREVIATION)).thenReturn(cities);

        List<PropertyView> properties = propertyViewService.getViewProperties(DEF_LANG_ABBREVIATION);

        Integer expectedSize = 1;

        assertEquals(expectedSize, properties.size());

        verify(propertyRegRepository, times(1)).findAllByOrderByIdDesc();

        verify(cachedCatalogBookClient, times(1)).getPropertyRoomTypes(REF_PROP_CAT_ID, DEF_LANG_ABBREVIATION);
        verify(cachedCatalogBookClient, times(1)).getAllCountries(DEF_LANG_ABBREVIATION);
        verify(cachedCatalogBookClient, times(1)).getAllCities(BELARUS_COUNTRY_ID, DEF_LANG_ABBREVIATION);

        String expectedJson = "{\"hotelAboutData\":{\"address\":{\"country\":{\"id\":1,\"name\":\"Belarus\"},\"city\":{\"id\":10,\"name\":\"Minsk\"}}},\"hotelLayoutData\":{\"layoutArea\":{\"roomType\":{\"id\":9,\"name\":\"family\"},\"roomName\":{\"id\":388,\"name\":\"family view on sea\"}}}}";

        assertEquals(PROPERTY_ID, properties.get(0).getPropId());
        assertEquals(REF_PROP_CAT_ID, properties.get(0).getPropCatId());
        assertEquals(PropertyJsonType.HOTEL_JSON, properties.get(0).getPropJsonType());
        assertEquals(expectedJson, objectMapper.writeValueAsString(properties.get(0).getPropJson()));
    }

    @Test
    void testPropertyListSize() {

        Supplier<PropertyEntity> propertyEntityFunc = () -> {
            long propId = RandomUtils.nextLong();

            PropertyEntity propertyEntity = createPropertyEntity(createUserPropertyEntity());
            propertyEntity.setId(propId);

            return propertyEntity;
        };

        int expectedSize = 10;

        when(propertyRegRepository.findAllByOrderByIdDesc())
                .thenReturn(Stream.generate(propertyEntityFunc).limit(expectedSize).toList());

        List<PropertyView> properties = propertyViewService.getViewProperties(DEF_LANG_ABBREVIATION);

        assertEquals(expectedSize, properties.size());

        verify(propertyRegRepository, times(1)).findAllByOrderByIdDesc();
        verify(cachedCatalogBookClient, times(expectedSize)).getPropertyRoomTypes(any(), any());
        verify(cachedCatalogBookClient, times(expectedSize)).getAllCountries(any());
        verify(cachedCatalogBookClient, times(expectedSize)).getAllCities(any(), any());
    }

    private Country createCountry(Long id, String name) {
        Country country = new Country();
        country.setId(id);
        country.setTsName(name);
        return country;
    }

    private City createCity(Long id, String name) {
        City city = new City();
        city.setId(id);
        city.setTsName(name);
        return city;
    }

    @Test
    void testQueryFakeParams() {
        String cc = "Poland,Warsaw,Fake,Fake,Fake";

        long POLAND_ID = 1;
        long WARSAW_ID = 11;

        Country poland = createCountry(POLAND_ID, "Poland");
        City warsaw = createCity(WARSAW_ID, "Warsaw");

        PropertyEntity propWarsawEntity = createPropertyEntity(1001L, POLAND_ID, WARSAW_ID, createUserPropertyEntity());

        when(cachedCatalogBookClient.getPropertyCategory(any())).thenReturn(hotelPropertyCategory);
        when(cachedCatalogBookClient.getAllCountries(any())).thenReturn(List.of(poland));
        when(cachedCatalogBookClient.getAllCities(eq(POLAND_ID), any())).thenReturn(List.of(warsaw));
        when(propertyRegRepository.findAllByOrderByIdDesc()).thenReturn(List.of(propWarsawEntity));

        List<PropertyView> properties = propertyViewService.queryViewProperties("en", cc, null);

        assertEquals(1, properties.size());

        verify(cachedCatalogBookClient, times(1)).getPropertyCategory(any());
        verify(cachedCatalogBookClient, times(2)).getAllCountries(any());
        verify(cachedCatalogBookClient, times(2)).getAllCities(eq(POLAND_ID), any());
        verify(propertyRegRepository, times(1)).findAllByOrderByIdDesc();

        HotelProperty property = objectMapper.convertValue(properties.get(0).getPropJson(), HotelProperty.class);

        assertEquals("Poland", property.getHotelAboutData().getAddress().getCountry().getName());
        assertEquals("Warsaw", property.getHotelAboutData().getAddress().getCity().getName());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "'   ';0",             // ?cc=___               (spaced country)
            "'   Ukraine   ';2",   // ?cc=___Ukraine___     (left and right spaced country)
            ";0",                  // ?cc=                  (no country)
            "Ukraine;2",           // ?cc=Ukraine
            "ukraine;2",           // ?cc=ukraine           (lowercase)
            "UKRAINE;2",           // ?cc=UKRAINE           (uppercase)
            "Poland;3",            // ?cc=Poland
            "Польша;0",            // ?cc=Польша            (russian letters)
            "Netherlands;0",       // ?cc=Netherlands       (unknown country)
    }, delimiter = ';')
    void testQueryWhenCounty(String ccCountry, Integer ccCount) {

        long UKRAINE_ID = 1;
        long POLAND_ID = 2;
        long NETHERLANDS_ID = 3;

        long KIEV_ID = 101;
        long LVOV_ID = 102;
        long WARSAW_ID = 103;
        long LODZ_ID = 104;
        long POZNAN_ID = 105;

        Country ukraine = createCountry(UKRAINE_ID, "Ukraine");
        Country poland = createCountry(POLAND_ID, "Poland");
        Country netherlands = createCountry(NETHERLANDS_ID, "Netherlands");

        City kiev = createCity(KIEV_ID, "Kiev");
        City lvov = createCity(LVOV_ID, "Lvov");
        City warsaw = createCity(WARSAW_ID, "Warsaw");
        City lodz = createCity(LODZ_ID, "Lodz");
        City poznan = createCity(POZNAN_ID, "Poznan");

        PropertyEntity propKievEntity = createPropertyEntity(1001L, UKRAINE_ID, KIEV_ID, createUserPropertyEntity());
        PropertyEntity propLvovEntity = createPropertyEntity(1002L, UKRAINE_ID, LVOV_ID, createUserPropertyEntity());

        PropertyEntity propWarshawEntity = createPropertyEntity(1003L, POLAND_ID, WARSAW_ID, createUserPropertyEntity());
        PropertyEntity propLodzEntity = createPropertyEntity(1004L, POLAND_ID, LODZ_ID, createUserPropertyEntity());
        PropertyEntity propPoznanEntity = createPropertyEntity(1005L, POLAND_ID, POZNAN_ID, createUserPropertyEntity());

        // * no properties in the Netherlands

        /*
            when lang is null means relying on default language (english)
        */

        when(cachedCatalogBookClient.getPropertyCategory(any())).thenReturn(hotelPropertyCategory);
        when(cachedCatalogBookClient.getAllCountries(any())).thenReturn(List.of(ukraine, poland, netherlands));

        when(cachedCatalogBookClient.getAllCities(eq(UKRAINE_ID), any())).thenReturn(List.of(kiev, lvov));
        when(cachedCatalogBookClient.getAllCities(eq(POLAND_ID), any())).thenReturn(List.of(warsaw, lodz, poznan));
        when(cachedCatalogBookClient.getAllCities(eq(NETHERLANDS_ID), any())).thenReturn(List.of());

        when(propertyRegRepository.findAllByOrderByIdDesc()).thenReturn(List.of(
                propKievEntity,
                propLvovEntity,
                propWarshawEntity,
                propLodzEntity,
                propPoznanEntity));

        List<PropertyView> properties = propertyViewService.queryViewProperties("en", ccCountry, null);

        assertEquals(ccCount, properties.size());

        String ccParam = Optional.ofNullable(ccCountry).flatMap(v -> Optional.of(v.trim())).orElse(null);

//        verify(cachedCatalogBookClient, times(ccCount)).getPropertyCategory(any());
        verify(cachedCatalogBookClient, times(Strings.isBlank(ccParam) ? 0 : ccCount + 1)).getAllCountries(any());
        verify(cachedCatalogBookClient, times(ccCount)).getAllCities(any(), any());

        boolean isCountryExist = Stream.of(ukraine, poland, netherlands)
                .anyMatch(country -> country.getTsName().equalsIgnoreCase(ccParam));

        verify(propertyRegRepository, times(isCountryExist ? 1 : 0)).findAllByOrderByIdDesc();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "'   ';'   ';0",                    // ?cc=___,___                   (spaced country and city)
            "'   ';Kiev;0",                     // ?cc=___,Kiev                  (country name is empty spaced string)
            "Poland;'   ';0",                   // ?cc=Poland,____               (spaced city)
            "'   Poland   ';Warsaw;1",          // ?cc=___Poland___,Warsaw       (country has spaces)
            "Poland;'   Warsaw   ';1",          // ?cc=Poland,___Warsaw___       (city has spaces)
            "'   Poland   ';'   Warsaw   ';1",  // ?cc=___Poland___,___Warsaw___ (country and city has spaces)
            ";Kiev;0",                          // ?cc=,Kiev                     (no country)
            ";;0",                              // ?cc=,                         (no country, no city)
            "Ukraine;Kiev;1",                   // ?cc=Ukraine,Kiev
            "Ukraine;Nikopol;0",                // ?cc=Ukraine,Nikopol           (unknown city)
            "UKRAINE;KIEV;1",                   // ?cc=UKRAINE,KIEV              (uppercase)
            "ukraine;kiev;1",                   // ?cc=ukraine,kiev              (lowercase)
            "Ukraine;;0",                       // ?cc=Ukraine,                  (no city)
            "Poland;Warsaw;1",                  // ?cc=Poland,Warsaw
            "Netherlands;Waalwijk;0",           // ?cc=Netherlands,Waalwijk      (unknown country and city)
            "Germany;;0"                        // ?cc=Germany,Berlin            (unknown country)
    }, delimiter = ';')
    void testQueryWhenCountyCity(String ccCountry, String ccCity, Integer ccCount) {

        long UKRAINE_ID = 1;
        long POLAND_ID = 2;
        long NETHERLANDS_ID = 3;

        long KIEV_ID = 101;
        long LVOV_ID = 102;
        long WARSAW_ID = 103;
        long LODZ_ID = 104;
        long POZNAN_ID = 105;

        Country ukraine = createCountry(UKRAINE_ID, "Ukraine");
        Country poland = createCountry(POLAND_ID, "Poland");
        Country netherlands = createCountry(NETHERLANDS_ID, "Netherlands");

        City kiev = createCity(KIEV_ID, "Kiev");
        City lvov = createCity(LVOV_ID, "Lvov");
        City warsaw = createCity(WARSAW_ID, "Warsaw");
        City lodz = createCity(LODZ_ID, "Lodz");
        City poznan = createCity(POZNAN_ID, "Poznan");

        PropertyEntity propKievEntity = createPropertyEntity(1001L, UKRAINE_ID, KIEV_ID, createUserPropertyEntity());
        PropertyEntity propLvovEntity = createPropertyEntity(1002L, UKRAINE_ID, LVOV_ID, createUserPropertyEntity());

        PropertyEntity propWarshawEntity = createPropertyEntity(1003L, POLAND_ID, WARSAW_ID, createUserPropertyEntity());
        PropertyEntity propLodzEntity = createPropertyEntity(1004L, POLAND_ID, LODZ_ID, createUserPropertyEntity());
        PropertyEntity propPoznanEntity = createPropertyEntity(1005L, POLAND_ID, POZNAN_ID, createUserPropertyEntity());

        // * no properties in the Netherlands

        /*
            when lang is null means relying on default language (english)
        */

        when(cachedCatalogBookClient.getPropertyCategory(any())).thenReturn(hotelPropertyCategory);
        when(cachedCatalogBookClient.getAllCountries(any())).thenReturn(List.of(ukraine, poland, netherlands));

        when(cachedCatalogBookClient.getAllCities(eq(UKRAINE_ID), any())).thenReturn(List.of(kiev, lvov));
        when(cachedCatalogBookClient.getAllCities(eq(POLAND_ID), any())).thenReturn(List.of(warsaw, lodz, poznan));
        when(cachedCatalogBookClient.getAllCities(eq(NETHERLANDS_ID), any())).thenReturn(List.of());

        when(propertyRegRepository.findAllByOrderByIdDesc()).thenReturn(List.of(
                propKievEntity,
                propLvovEntity,
                propWarshawEntity,
                propLodzEntity,
                propPoznanEntity));

        String qCountry = Optional.ofNullable(ccCountry).orElse(Strings.EMPTY);
        String qCity = Optional.ofNullable(ccCity).orElse(Strings.EMPTY);

        String ccStr = qCountry.concat(",").concat(qCity);

        List<PropertyView> properties = propertyViewService.queryViewProperties("en", ccStr, null);

        assertEquals(ccCount, properties.size());

        verify(cachedCatalogBookClient, times(Strings.isBlank(qCountry)
                ? 0
                : ccCount + 1)).getAllCountries(any());

        verify(cachedCatalogBookClient, times(Strings.isBlank(qCountry) || Strings.isBlank(qCity)
                ? 0
                : ccCount + 1)).getAllCities(any(), any());

//        verify(cachedCatalogBookClient, times(Strings.isBlank(qCountry) || Strings.isBlank(qCity)
//                ? 0
//                : ccCount + 1)).getPropertyCategory(any());

        // call findAllByOrderByIdDesc when only country and city exist

        boolean isCountryExist = Stream.of(ukraine, poland, netherlands)
                .anyMatch(country -> country.getTsName().equalsIgnoreCase(qCountry.trim()));

        boolean isCityExist = Stream.of(kiev, lvov, warsaw, lodz, poznan)
                .anyMatch(city -> city.getTsName().equalsIgnoreCase(qCity.trim()));

        verify(propertyRegRepository, times(isCountryExist && isCityExist ? 1 : 0)).findAllByOrderByIdDesc();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "en; family; family view on sea; Poland; Warsaw",
            "ru; Семейный; Семейный с видом на море; Польша; Варшава"
    }, delimiter = ';')
    void testQueryPropertiesWhenCountyCity(String lang, String roomTypeName, String roomName, String countryName,
                                           String cityName) throws JsonProcessingException {

        List<RoomName> roomNames = List.of(new RoomName(388L, roomName));
        List<RoomType> roomType = List.of(new RoomType(9L, roomTypeName, roomNames));

        PropertyEntity propertyEntity = createPropertyEntity(createUserPropertyEntity());

        /*
            when lang is null means relying on default language (english)
        */

        when(cachedCatalogBookClient.getPropertyCategory(any())).thenReturn(hotelPropertyCategory);

        when(cachedCatalogBookClient.getAllCountries(null)).thenReturn(List.of(createCountry(1L, "Poland")));
        when(cachedCatalogBookClient.getAllCities(any(), eq(null))).thenReturn(List.of(createCity(10L, "Warsaw")));

        when(cachedCatalogBookClient.getAllCountries(lang)).thenReturn(List.of(createCountry(1L, countryName)));
        when(cachedCatalogBookClient.getAllCities(any(), eq(lang))).thenReturn(List.of(createCity(10L, cityName)));

        when(propertyRegRepository.findAllByOrderByIdDesc()).thenReturn(List.of(propertyEntity));
        when(cachedCatalogBookClient.getPropertyRoomTypes(REF_PROP_CAT_ID, lang)).thenReturn(roomType);

        /*
            query does not depend on the language, searching always with english
         */

        List<PropertyView> properties = propertyViewService.queryViewProperties(lang, "Poland,Warsaw", null);

        assertEquals(1, properties.size());

        PropertyView expectedProperty = new PropertyView();

        expectedProperty.setPropId(propertyEntity.getId());

        String expectedJson = "{\"hotelAboutData\":{\"address\":{\"country\":{\"id\":1,\"name\":\"" + countryName +
                "\"},\"city\":{\"id\":10,\"name\":\"" + cityName +
                "\"}}},\"hotelLayoutData\":{\"layoutArea\":{\"roomType\":{\"id\":9,\"name\":\"" + roomTypeName +
                "\"},\"roomName\":{\"id\":388,\"name\":\"" + roomName +
                "\"}}}}";

        HotelProperty hotelProperty = objectMapper.readValue(expectedJson, HotelProperty.class);

        expectedProperty.setPropJson(hotelProperty);
        expectedProperty.setPropJsonType(propertyEntity.getPropertyJsonType());
        expectedProperty.setPropCatId(propertyEntity.getUserPropertyEntity().getRefPropCatId());

        verify(cachedCatalogBookClient, times(1)).getPropertyCategory(any());
        verify(cachedCatalogBookClient, times(1)).getAllCountries(null);
        verify(cachedCatalogBookClient, times(1)).getAllCities(any(), eq(null));

        verify(cachedCatalogBookClient, times(1)).getAllCountries(lang);
        verify(cachedCatalogBookClient, times(1)).getAllCities(any(), eq(lang));

        verify(propertyRegRepository, times(1)).findAllByOrderByIdDesc();
        verify(cachedCatalogBookClient, times(1)).getPropertyRoomTypes(REF_PROP_CAT_ID, lang);

        assertEquals(expectedProperty, properties.get(0));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "HOTEL;1",
            "HOSTEL;0",
            "APART_HOTEL;0",
    }, delimiter = ';')
    void testPropertyCategoryWhenCountry(String pCatType, Integer pCount) {
        PropertyEntity propertyEntity = createPropertyEntity(createUserPropertyEntity());

        when(cachedCatalogBookClient.getAllCountries(null)).thenReturn(List.of(belarusCountry));
        when(cachedCatalogBookClient.getPropertyCategory(any())).thenReturn(hotelPropertyCategory);
        when(propertyRegRepository.findAllByOrderByIdDesc()).thenReturn(List.of(propertyEntity));

        List<PropertyView> properties = propertyViewService.queryViewProperties(DEF_LANG_ABBREVIATION,
                "Belarus", CategoryTypes.valueOf(pCatType));

        assertEquals(pCount, properties.size());

        verify(cachedCatalogBookClient, times(1)).getAllCountries(null);
        verify(cachedCatalogBookClient, times(1)).getPropertyCategory(any());
        verify(propertyRegRepository, times(1)).findAllByOrderByIdDesc();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "HOTEL;1",
            "HOSTEL;0",
            "APART_HOTEL;0",
    }, delimiter = ';')
    void testPropertyCategoryWhenCountryCity(String pCatType, Integer pCount) {
        PropertyEntity propertyEntity = createPropertyEntity(createUserPropertyEntity());

        when(cachedCatalogBookClient.getAllCountries(null)).thenReturn(List.of(belarusCountry));
        when(cachedCatalogBookClient.getAllCities(BELARUS_COUNTRY_ID, null)).thenReturn(List.of(minskCity));
        when(cachedCatalogBookClient.getPropertyCategory(any())).thenReturn(hotelPropertyCategory);
        when(propertyRegRepository.findAllByOrderByIdDesc()).thenReturn(List.of(propertyEntity));

        List<PropertyView> properties = propertyViewService.queryViewProperties(DEF_LANG_ABBREVIATION,
                "Belarus,Minsk", CategoryTypes.valueOf(pCatType));

        assertEquals(pCount, properties.size());

        verify(cachedCatalogBookClient, times(1)).getAllCountries(null);
        verify(cachedCatalogBookClient, times(1)).getAllCities(BELARUS_COUNTRY_ID, null);

        verify(cachedCatalogBookClient, times(1)).getPropertyCategory(any());
        verify(propertyRegRepository, times(1)).findAllByOrderByIdDesc();
    }
}
