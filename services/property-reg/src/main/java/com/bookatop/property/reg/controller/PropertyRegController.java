package com.bookatop.property.reg.controller;

import com.bookatop.property.reg.model.PropertyAdd;
import com.bookatop.property.reg.model.PropertyResponse;
import com.bookatop.property.reg.service.PropertyRegService;
import com.bookatop.security.annotation.AccessUserRoles;
import com.bookatop.security.enums.UserRoles;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "property-reg/registration", produces = MediaType.APPLICATION_JSON_VALUE)
public class PropertyRegController {

    public static final String PROPERTY_IS_ADDED = "Property is added";

    private final PropertyRegService propertyRegService;

    public PropertyRegController(PropertyRegService propertyRegService) {
        this.propertyRegService = propertyRegService;
    }

    @PostMapping("/add")
    @AccessUserRoles(UserRoles.USER)
    public ResponseEntity<PropertyResponse> addProperty(@Valid @RequestBody PropertyAdd request) {
        propertyRegService.addProperty(request);
        return ResponseEntity.ok(new PropertyResponse(PROPERTY_IS_ADDED));
    }
}
