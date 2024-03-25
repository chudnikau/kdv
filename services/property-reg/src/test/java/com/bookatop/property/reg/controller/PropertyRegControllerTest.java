package com.bookatop.property.reg.controller;

import com.bookatop.property.reg.api.enums.PropertyJsonType;
import com.bookatop.property.reg.config.SpringTestConfig;
import com.bookatop.property.reg.exception.PropertyRegException;
import com.bookatop.property.reg.model.PropertyAdd;
import com.bookatop.property.reg.model.PropertyResponse;
import com.bookatop.property.reg.model.UserProperty;
import com.bookatop.property.reg.service.PropertyRegService;
import com.bookatop.security.enums.UserRoles;
import com.bookatop.security.request.GatewayHeaders;
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
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SpringTestConfig.class)
class PropertyRegControllerTest {

    private static final String PROPERTY_REG_ADD = "/property-reg/registration/add";

    private static final long REF_PROP_TYPE_ID = 1113243L;

    private static final long REF_PROP_CAT_ID = 2228764L;

    private static final long REF_USER_ID = 33329034L;

    private static final String VALID_PROP_JSON = "{'value':'5455450'}";

    private static final PropertyJsonType VALID_PROP_JSON_TYPE = PropertyJsonType.MOTEL_JSON;

    @MockBean
    private PropertyRegService propertyRegService;

    @Autowired
    private TestRestTemplate restTemplate;

    private PropertyAdd propRequest;

    private <T, R> ResponseEntity<R> postRequest(String url, T body, Class<R> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(GatewayHeaders.ROLES, UserRoles.USER.name());
        headers.add(GatewayHeaders.UID, String.valueOf(REF_USER_ID));

        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, httpEntity, responseType);
    }

    @BeforeEach
    void beforeEach() {
        UserProperty userProperty = new UserProperty(REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);
        propRequest = new PropertyAdd(VALID_PROP_JSON, VALID_PROP_JSON_TYPE, userProperty);
    }

    @Test
    void testPropertyResponseStatus() {
        ResponseEntity<PropertyResponse> response =
                postRequest(PROPERTY_REG_ADD, propRequest, PropertyResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testPropertyResponseBody() {
        ResponseEntity<PropertyResponse> response =
                postRequest(PROPERTY_REG_ADD, propRequest, PropertyResponse.class);

        assertEquals(PropertyRegController.PROPERTY_IS_ADDED,
                Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void testPropertyServiceCall() {
        postRequest(PROPERTY_REG_ADD, propRequest, PropertyResponse.class);

        verify(propertyRegService, times(1)).addProperty(any());
    }

    @Test
    void testPropertyBadResponseStatus() {
        doThrow(PropertyRegException.class).when(propertyRegService).addProperty(any());

        ResponseEntity<PropertyResponse> response =
                postRequest(PROPERTY_REG_ADD, propRequest, PropertyResponse.class);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }
}
