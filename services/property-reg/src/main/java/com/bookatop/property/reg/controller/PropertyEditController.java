package com.bookatop.property.reg.controller;


import com.bookatop.property.reg.model.PropertyEditItem;
import com.bookatop.property.reg.model.PropertyResponse;
import com.bookatop.property.reg.model.PropertyUpdate;
import com.bookatop.property.reg.service.PropertyEditService;
import com.bookatop.security.annotation.AccessUserRoles;
import com.bookatop.security.enums.UserRoles;
import com.bookatop.security.providers.HttpShareSensitiveProvider;
import com.bookatop.security.request.GatewayHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "property-reg/edit", produces = MediaType.APPLICATION_JSON_VALUE)
public class PropertyEditController {

    public static final String PROPERTY_ACTIVATED = "Property is activated";

    public static final String PROPERTY_DEACTIVATED = "Property is deactivated";

    public static final String PROPERTY_UPDATED = "Property is updated";

    private final PropertyEditService propertyEditService;

    public PropertyEditController(PropertyEditService propertyEditService) {
        this.propertyEditService = propertyEditService;
    }

    @PostMapping("/{propId}/activate")
    @AccessUserRoles(UserRoles.USER)
    public ResponseEntity<PropertyResponse> activateProperty(@RequestHeader(GatewayHeaders.UID) Long userId,
                                                             @PathVariable Long propId) {
        propertyEditService.activeProperty(userId, propId, true);
        return ResponseEntity.ok(new PropertyResponse(PROPERTY_ACTIVATED));
    }

    @PostMapping("/{propId}/deactivate")
    @AccessUserRoles(UserRoles.USER)
    public ResponseEntity<PropertyResponse> deactivateProperty(@RequestHeader(GatewayHeaders.UID) Long userId,
                                                               @PathVariable Long propId) {
        propertyEditService.activeProperty(userId, propId, false);
        return ResponseEntity.ok(new PropertyResponse(PROPERTY_DEACTIVATED));
    }

    @PostMapping("/{propId}/update")
    @AccessUserRoles(UserRoles.USER)
    public ResponseEntity<PropertyResponse> updateProperty(@RequestHeader(GatewayHeaders.UID) Long userId,
                                                           @PathVariable Long propId,
                                                           @Valid @RequestBody PropertyUpdate propUpdate) {
        propertyEditService.updateProperty(userId, propId, propUpdate);
        return ResponseEntity.ok(new PropertyResponse(PROPERTY_UPDATED));
    }

    @GetMapping("/list")
    @AccessUserRoles(UserRoles.USER)
    public ResponseEntity<List<PropertyEditItem>> getEditPropertyList(@RequestHeader(GatewayHeaders.UID) Long userId,
                                                                      @RequestParam(required = false) String lang) {
        return ResponseEntity.ok(propertyEditService.getEditPropertyList(userId, lang));
    }

    @GetMapping("/list/{propId}")
    @AccessUserRoles(UserRoles.USER)
    public ResponseEntity<HttpShareSensitiveProvider> getEditProperty(@RequestHeader(GatewayHeaders.UID) Long userId,
                                                                      @PathVariable Long propId,
                                                                      @RequestParam(required = false) String lang) {
        return ResponseEntity.ok(new HttpShareSensitiveProvider(propertyEditService.getEditProperty(userId, propId, lang)));
    }
}
