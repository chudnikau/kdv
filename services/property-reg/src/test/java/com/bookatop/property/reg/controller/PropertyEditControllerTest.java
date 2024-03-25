package com.bookatop.property.reg.controller;

import com.bookatop.catalog.book.api.enums.CategoryTypes;
import com.bookatop.property.reg.api.enums.PropertyJsonType;
import com.bookatop.property.reg.config.SpringTestConfig;
import com.bookatop.property.reg.exception.PropertyRegException;
import com.bookatop.property.reg.model.PropertyEditView;
import com.bookatop.property.reg.model.PropertyResponse;
import com.bookatop.property.reg.model.PropertyUpdate;
import com.bookatop.property.reg.model.WithImplSensitiveContactData;
import com.bookatop.property.reg.service.PropertyEditService;
import com.bookatop.security.enums.UserRoles;
import com.bookatop.security.request.GatewayHeaders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SpringTestConfig.class)
class PropertyEditControllerTest {

    private static final long PROPERTY_ID = 748456L;

    private static final long REF_USER_ID = 6892321L;

    private static final long REF_PROP_CAT_ID = 222567L;

    private static final String VALID_PROP_JSON = "{'value':'5455450'}";

    private static final PropertyJsonType VALID_PROP_JSON_TYPE = PropertyJsonType.MOTEL_JSON;

    private static final String CUSTOM_PROPERTY_REG_UPDATE = "/property-reg/edit/748456/update";

    private static final String CUSTOM_PROPERTY_REG_ACTIVATE = "/property-reg/edit/748456/activate";

    private static final String CUSTOM_PROPERTY_REG_DEACTIVATE = "/property-reg/edit/748456/deactivate";

    private static final String CUSTOM_PROPERTY_REG_EDIT_VIEW = "/property-reg/edit/list/748456";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private PropertyEditService propertyEditService;

    private PropertyUpdate propUpdate;

    @Autowired
    private ObjectMapper objectMapper;

    private <T, R> ResponseEntity<R> getEditRequest(String url, Class<R> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(GatewayHeaders.ROLES, UserRoles.USER.name());
        headers.add(GatewayHeaders.UID, String.valueOf(REF_USER_ID));

        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType);
    }

    private <T, R> ResponseEntity<R> postEditRequest(String url, T body, Class<R> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(GatewayHeaders.ROLES, UserRoles.USER.name());
        headers.add(GatewayHeaders.UID, String.valueOf(REF_USER_ID));

        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, httpEntity, responseType);
    }

    @BeforeEach
    void beforeEach() {
        propUpdate = new PropertyUpdate(VALID_PROP_JSON, VALID_PROP_JSON_TYPE, REF_PROP_CAT_ID, CategoryTypes.HOTEL);
    }

    @Test
    void testActivateProperty() {
        ResponseEntity<PropertyResponse> response =
                postEditRequest(CUSTOM_PROPERTY_REG_ACTIVATE, null, PropertyResponse.class);

        assertEquals(PropertyEditController.PROPERTY_ACTIVATED,
                Objects.requireNonNull(response.getBody()).getMessage());

        verify(propertyEditService, times(1))
                .activeProperty(REF_USER_ID, PROPERTY_ID, true);
    }

    @Test
    void testDeactivateProperty() {
        ResponseEntity<PropertyResponse> response =
                postEditRequest(CUSTOM_PROPERTY_REG_DEACTIVATE, null, PropertyResponse.class);

        assertEquals(PropertyEditController.PROPERTY_DEACTIVATED,
                Objects.requireNonNull(response.getBody()).getMessage());

        verify(propertyEditService, times(1))
                .activeProperty(REF_USER_ID, PROPERTY_ID, false);
    }

    @Test
    void testActivatePropertyException() {
        doThrow(PropertyRegException.class).when(propertyEditService)
                .activeProperty(anyLong(), anyLong(), any());

        ResponseEntity<PropertyResponse> response =
                postEditRequest(CUSTOM_PROPERTY_REG_ACTIVATE, null, PropertyResponse.class);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    void testDeactivatePropertyException() {
        doThrow(PropertyRegException.class).when(propertyEditService)
                .activeProperty(anyLong(), anyLong(), any());

        ResponseEntity<PropertyResponse> response =
                postEditRequest(CUSTOM_PROPERTY_REG_DEACTIVATE, null, PropertyResponse.class);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    void testUpdatePropertyStatus() {
        ResponseEntity<PropertyResponse> response =
                postEditRequest(CUSTOM_PROPERTY_REG_UPDATE, propUpdate, PropertyResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdatePropertyBody() {
        ResponseEntity<PropertyResponse> response =
                postEditRequest(CUSTOM_PROPERTY_REG_UPDATE, propUpdate, PropertyResponse.class);

        assertEquals(PropertyEditController.PROPERTY_UPDATED,
                Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void testSensitiveEditBody() throws JsonProcessingException {
        WithImplSensitiveContactData sensitiveData = new WithImplSensitiveContactData();

        sensitiveData.setName("customer");
        sensitiveData.setBankAccountNr("4732749382749382");
        sensitiveData.setPinCode(8968);

        PropertyEditView propertyEditView =
                new PropertyEditView(sensitiveData, PropertyJsonType.HOTEL_JSON, REF_PROP_CAT_ID, CategoryTypes.HOTEL);

        when(propertyEditService.getEditProperty(eq(REF_USER_ID), eq(PROPERTY_ID), any()))
                .thenReturn(propertyEditView);

        ResponseEntity<PropertyEditView> response =
                getEditRequest(CUSTOM_PROPERTY_REG_EDIT_VIEW, PropertyEditView.class);

        verify(propertyEditService, times(1)).getEditProperty(eq(REF_USER_ID), eq(PROPERTY_ID), any());

        PropertyEditView body = response.getBody();
        assertTrue(Objects.nonNull(body));

        String expectedJSON = "{\"name\":\"customer\",\"bankAccountNr\":\"4732749382749382\",\"pinCode\":8968}";

        assertEquals(expectedJSON, objectMapper.writeValueAsString(body.getPropJson()));
        assertEquals(PropertyJsonType.HOTEL_JSON, body.getPropJsonType());
    }

    @Test
    void testUpdatePropertyCall() {
        postEditRequest(CUSTOM_PROPERTY_REG_UPDATE, propUpdate, PropertyResponse.class);

        verify(propertyEditService, times(1))
                .updateProperty(eq(REF_USER_ID), eq(PROPERTY_ID), argThat(v -> {
                    assertEquals(VALID_PROP_JSON, v.getPropJson());
                    assertEquals(VALID_PROP_JSON_TYPE, v.getPropJsonType());
                    return true;
                }));
    }

    @Test
    void testUpdatePropertyException() {
        doThrow(PropertyRegException.class).when(propertyEditService)
                .updateProperty(eq(REF_USER_ID), eq(PROPERTY_ID), any());

        ResponseEntity<PropertyResponse> response =
                postEditRequest(CUSTOM_PROPERTY_REG_UPDATE, propUpdate, PropertyResponse.class);

        verify(propertyEditService, times(1))
                .updateProperty(eq(REF_USER_ID), eq(PROPERTY_ID), any());

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

}
