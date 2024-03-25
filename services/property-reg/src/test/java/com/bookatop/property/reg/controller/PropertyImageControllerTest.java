package com.bookatop.property.reg.controller;

import com.bookatop.property.reg.api.enums.PropertyJsonType;
import com.bookatop.property.reg.config.SpringTestConfig;
import com.bookatop.property.reg.model.PropertyAdd;
import com.bookatop.property.reg.model.UserProperty;
import com.bookatop.security.enums.UserRoles;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SpringTestConfig.class)
class PropertyImageControllerTest {

    private static final long REF_PROP_TYPE_ID = 111L;

    private static final long REF_PROP_CAT_ID = 222L;

    private static final long REF_USER_ID = 333L;

    private static final String VALID_PROP_JSON = "{'value':'5719066'}";

    private static final PropertyJsonType VALID_PROP_JSON_TYPE = PropertyJsonType.HOSTEL_JSON;

    @Autowired
    private TestRestTemplate restTemplate;

    private PropertyAdd propRequest;

    private <T, R> ResponseEntity<R> postRequest(String url, T body, Class<R> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ROLES", UserRoles.USER.name());

        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, httpEntity, responseType);
    }

    @BeforeEach
    void beforeEach() {
        UserProperty userProperty = new UserProperty(REF_PROP_TYPE_ID, REF_PROP_CAT_ID, REF_USER_ID);
        propRequest = new PropertyAdd(VALID_PROP_JSON, VALID_PROP_JSON_TYPE, userProperty);
    }
}