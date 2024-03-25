package com.bookatop.property.reg.service;

import com.bookatop.property.reg.api.enums.PropertyJsonType;
import com.bookatop.property.reg.clients.CachedCatalogBookClient;
import com.bookatop.property.reg.entity.PropertyEntity;
import com.bookatop.property.reg.model.PropertyAdd;
import com.bookatop.property.reg.model.UserProperty;
import com.bookatop.property.reg.repository.PropertyRegRepository;
import com.bookatop.property.reg.repository.UserPropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application.properties")
class PropertyRegServiceTest {

    private static final long REF_PROP_TYPE_ID = 111L;

    private static final long REF_PROP_CAT_ID = 222L;

    private static final long REF_USER_ID = 333L;

    private static final long PROPERTY_ID = 555L;

    private static final String VALID_PROP_JSON = "{\"name\":\"alex\"}";

    private static final PropertyJsonType VALID_PROP_JSON_TYPE = PropertyJsonType.GUEST_HOUSE_JSON;

    private static final PropertyJsonType EMPTY_PROP_JSON_TYPE = PropertyJsonType.HOSTEL_JSON;

    private static final String INVALID_PROP_JSON = "{JSON}";

    private static final String EMPTY_PROP_JSON = "";

    private static final String SPACE_PROP_JSON = " ";

    @Autowired
    private PropertyRegService propertyRegService;

    @MockBean
    private PropertyRegRepository propertyRegRepository;

    @MockBean
    private UserPropertyRepository userPropertyRepository;

    @MockBean
    private CachedCatalogBookClient cachedCatalogBookClient;

    @BeforeEach
    void beforeEach() {
    }

    @Test
    void testPropertyRegCall() {
        PropertyEntity propertyEntity = new PropertyEntity();
        propertyEntity.setId(PROPERTY_ID);
        propertyEntity.setPropertyJson(VALID_PROP_JSON);
        propertyEntity.setPropertyJsonType(VALID_PROP_JSON_TYPE);

        when(propertyRegRepository.save(any())).thenReturn(propertyEntity);

        UserProperty userProperty = new UserProperty(
                REF_PROP_TYPE_ID,
                REF_PROP_CAT_ID,
                REF_USER_ID
        );

        PropertyAdd propRequest = new PropertyAdd(
                VALID_PROP_JSON,
                VALID_PROP_JSON_TYPE,
                userProperty
        );

        propertyRegService.addProperty(propRequest);

        verify(propertyRegRepository, times(1)).save(argThat(
                argument -> {
                    assertNull(argument.getId());
                    assertEquals(VALID_PROP_JSON, argument.getPropertyJson());
                    assertEquals(VALID_PROP_JSON_TYPE, argument.getPropertyJsonType());
                    return true;
                }
        ));

        verify(userPropertyRepository, times(1)).save(argThat(
                argument -> {
                    assertEquals(PROPERTY_ID, argument.getPropertyId());
                    assertEquals(REF_PROP_TYPE_ID, argument.getRefPropTypeId());
                    assertEquals(REF_PROP_CAT_ID, argument.getRefPropCatId());
                    assertEquals(REF_USER_ID, argument.getRefUserId());
                    return true;
                }
        ));
    }

    @Test
    void testPropertyNull() {
        assertThrows(RuntimeException.class, () -> propertyRegService.addProperty(null));
    }

    @Test
    void testNullUserProperty() {
        PropertyAdd propRequest =
                new PropertyAdd(VALID_PROP_JSON, VALID_PROP_JSON_TYPE, null);
        assertThrows(RuntimeException.class, () ->
                propertyRegService.addProperty(propRequest));
    }

    @Test
    void testNullRefPropTypeId() {
        UserProperty userProperty =
                new UserProperty(null, REF_PROP_CAT_ID, REF_USER_ID);
        PropertyAdd propRequest =
                new PropertyAdd(VALID_PROP_JSON, VALID_PROP_JSON_TYPE, userProperty);
        assertThrows(RuntimeException.class, () ->
                propertyRegService.addProperty(propRequest));
    }

    @Test
    void testNullRefPropCatId() {
        UserProperty userProperty =
                new UserProperty(REF_PROP_TYPE_ID, null, REF_USER_ID);
        PropertyAdd propRequest =
                new PropertyAdd(VALID_PROP_JSON, VALID_PROP_JSON_TYPE, userProperty);
        assertThrows(RuntimeException.class, () ->
                propertyRegService.addProperty(propRequest));
    }

    @Test
    void testNullUserId() {
        UserProperty userProperty =
                new UserProperty(REF_PROP_TYPE_ID, REF_PROP_CAT_ID, null);
        PropertyAdd propRequest =
                new PropertyAdd(VALID_PROP_JSON, VALID_PROP_JSON_TYPE, userProperty);
        assertThrows(RuntimeException.class, () ->
                propertyRegService.addProperty(propRequest));
    }

    @Test
    void testNullPropJson() {
        UserProperty userProperty =
                new UserProperty(REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);
        PropertyAdd propRequest =
                new PropertyAdd(null, VALID_PROP_JSON_TYPE, userProperty);
        assertThrows(RuntimeException.class, () ->
                propertyRegService.addProperty(propRequest));
    }

    @Test
    void testNullPropJsonType() {
        UserProperty userProperty =
                new UserProperty(REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);
        PropertyAdd propRequest =
                new PropertyAdd(VALID_PROP_JSON, null, userProperty);
        assertThrows(RuntimeException.class, () ->
                propertyRegService.addProperty(propRequest));
    }

    @Test
    void testEmptyJson() {
        UserProperty userProperty =
                new UserProperty(REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);
        PropertyAdd propRequest =
                new PropertyAdd(EMPTY_PROP_JSON, VALID_PROP_JSON_TYPE, userProperty);

        assertThrows(RuntimeException.class, () ->
                propertyRegService.addProperty(propRequest));
    }

    @Test
    void testSpaceJson() {
        UserProperty userProperty =
                new UserProperty(REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);
        PropertyAdd propRequest =
                new PropertyAdd(SPACE_PROP_JSON, VALID_PROP_JSON_TYPE, userProperty);

        assertThrows(RuntimeException.class, () ->
                propertyRegService.addProperty(propRequest));
    }

    @Test
    void testInvalidJson() {
        UserProperty userProperty =
                new UserProperty(REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);
        PropertyAdd propRequest =
                new PropertyAdd(INVALID_PROP_JSON, VALID_PROP_JSON_TYPE, userProperty);

        assertThrows(RuntimeException.class, () ->
                propertyRegService.addProperty(propRequest));
    }

    @Test
    void testEmptyJsonType() {
        UserProperty userProperty =
                new UserProperty(REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);
        PropertyAdd propRequest =
                new PropertyAdd(VALID_PROP_JSON, EMPTY_PROP_JSON_TYPE, userProperty);

        assertThrows(RuntimeException.class, () ->
                propertyRegService.addProperty(propRequest));
    }
}
