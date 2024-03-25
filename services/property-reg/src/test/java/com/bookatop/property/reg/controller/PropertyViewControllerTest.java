package com.bookatop.property.reg.controller;

import com.bookatop.catalog.book.api.enums.CategoryTypes;
import com.bookatop.property.reg.api.enums.PropertyJsonType;
import com.bookatop.property.reg.api.model.PropertyView;
import com.bookatop.property.reg.config.SpringTestConfig;
import com.bookatop.property.reg.exception.PropertyRegException;
import com.bookatop.property.reg.model.ErrorResponse;
import com.bookatop.property.reg.model.PropertyAdd;
import com.bookatop.property.reg.model.PropertyEditView;
import com.bookatop.property.reg.model.WithImplSensitiveContactData;
import com.bookatop.property.reg.service.PropertyViewService;
import com.bookatop.security.enums.UserRoles;
import com.bookatop.security.request.GatewayHeaders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SpringTestConfig.class)
class PropertyViewControllerTest {

    private static final String PROPERTY_REG_LIST = "/property-reg/view/list";

    private static final String CUSTOM_PROPERTY_REG_VIEW = "/property-reg/view/list/748";

    private static final long REF_USER_ID = 333L;

    private static final String DEF_LANG_ABBREVIATION = "en";

    private static final String VALID_PROP_JSON = "{\"contact\":{\"name\":\"Сергей\",\"telNumber\":\"555555\",\"altTelNumber\":\"6666666\",\"email\":\"bk@mail.com\"}}";

    private static final long PROPERTY_ID = 748L;

    private static final long REF_PROP_CAT_ID = 111567L;

    private static final PropertyJsonType VALID_PROP_JSON_TYPE = PropertyJsonType.MOTEL_JSON;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private PropertyViewService propertyViewService;

    @Autowired
    private ObjectMapper objectMapper;

    private <R> ResponseEntity<R> getViewRequest(String url, UserRoles userRole, Class<R> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(GatewayHeaders.ROLES, userRole.name());
        headers.add(GatewayHeaders.UID, String.valueOf(REF_USER_ID));

        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType);
    }

    private <R> ResponseEntity<R> getViewRequest(String url, Class<R> responseType) {
        return getViewRequest(url, UserRoles.USER, responseType);
    }

    @Test
    void testPropertiesStatus() {
        ResponseEntity<PropertyAdd[]> response =
                getViewRequest(PROPERTY_REG_LIST, PropertyAdd[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testPropertiesCall() {
        getViewRequest(PROPERTY_REG_LIST, PropertyAdd[].class);

        verify(propertyViewService, times(1)).getViewProperties(any());
    }

    @Test
    void testListPropertyViewStatus() {
        ResponseEntity<PropertyEditView> response =
                getViewRequest(CUSTOM_PROPERTY_REG_VIEW, PropertyEditView.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testListPropertyViewBody() {
        PropertyView propertyView =
                new PropertyView(PROPERTY_ID, VALID_PROP_JSON, VALID_PROP_JSON_TYPE, REF_PROP_CAT_ID);

        when(propertyViewService.getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION))
                .thenReturn(propertyView);

        ResponseEntity<PropertyView> response =
                getViewRequest("/property-reg/view/list/748?lang=" + DEF_LANG_ABBREVIATION, PropertyView.class);

        verify(propertyViewService, times(1)).getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION);

        PropertyView body = response.getBody();
        assertTrue(Objects.nonNull(body));

        assertEquals(PROPERTY_ID, body.getPropId());
        assertEquals(VALID_PROP_JSON, body.getPropJson());
        assertEquals(VALID_PROP_JSON_TYPE, body.getPropJsonType());
        assertEquals(REF_PROP_CAT_ID, body.getPropCatId());
    }

    @Test
    void testSensitiveViewBody() throws JsonProcessingException {
        WithImplSensitiveContactData sensitiveData = new WithImplSensitiveContactData();

        sensitiveData.setName("customer");
        sensitiveData.setBankAccountNr("00112233445566");
        sensitiveData.setPinCode(4321);

        PropertyView propertyView =
                new PropertyView(PROPERTY_ID, sensitiveData, PropertyJsonType.HOTEL_JSON, REF_PROP_CAT_ID);

        when(propertyViewService.getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION))
                .thenReturn(propertyView);

        ResponseEntity<PropertyView> response =
                getViewRequest("/property-reg/view/list/748?lang=" + DEF_LANG_ABBREVIATION, PropertyView.class);

        verify(propertyViewService, times(1)).getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION);

        PropertyView body = response.getBody();
        assertTrue(Objects.nonNull(body));

        String expectedJSON = "{\"name\":\"customer\"}";

        assertEquals(expectedJSON, objectMapper.writeValueAsString(body.getPropJson()));
        assertEquals(PropertyJsonType.HOTEL_JSON, body.getPropJsonType());
    }

    @Test
    void testListPropertyViewException() {
        doThrow(PropertyRegException.class).when(propertyViewService)
                .getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION);

        ResponseEntity<PropertyView> response =
                getViewRequest("/property-reg/view/list/748?lang=" + DEF_LANG_ABBREVIATION, PropertyView.class);

        verify(propertyViewService, times(1))
                .getViewProperty(PROPERTY_ID, DEF_LANG_ABBREVIATION);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    void testQueryPropertyView() {
        ResponseEntity<PropertyView[]> response =
                getViewRequest("/property-reg/view/list?lang=" + DEF_LANG_ABBREVIATION + "&cc=Belarus,Minsk", PropertyView[].class);

        verify(propertyViewService, times(1))
                .queryViewProperties(DEF_LANG_ABBREVIATION, "Belarus,Minsk", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testPropertyCategory() {
        ResponseEntity<PropertyView[]> response =
                getViewRequest("/property-reg/view/list?lang=" + DEF_LANG_ABBREVIATION + "&cc=Belarus,Minsk&pcat_type=hotel", PropertyView[].class);

        verify(propertyViewService, times(1))
                .queryViewProperties(DEF_LANG_ABBREVIATION, "Belarus,Minsk", CategoryTypes.HOTEL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testQueryPropertyViewWithoutLanguage() {
        ResponseEntity<PropertyView[]> response =
                getViewRequest("/property-reg/view/list?cc=Belarus,Minsk", PropertyView[].class);

        verify(propertyViewService, times(1))
                .queryViewProperties(null, "Belarus,Minsk", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testQueryPropertyViewException() {
        doThrow(PropertyRegException.class).when(propertyViewService)
                .queryViewProperties(any(), any(), any());

        ResponseEntity<ErrorResponse> response =
                getViewRequest("/property-reg/view/list?lang=" + DEF_LANG_ABBREVIATION + "&cc=Belarus,Minsk", ErrorResponse.class);

        verify(propertyViewService, times(1))
                .queryViewProperties(DEF_LANG_ABBREVIATION, "Belarus,Minsk", null);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }
}
