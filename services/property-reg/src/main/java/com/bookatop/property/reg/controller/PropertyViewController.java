package com.bookatop.property.reg.controller;

import com.bookatop.catalog.book.api.enums.CategoryTypes;
import com.bookatop.property.reg.api.model.PropertyView;
import com.bookatop.property.reg.service.PropertyViewService;
import com.bookatop.security.annotation.AccessUserRoles;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "property-reg/view", produces = MediaType.APPLICATION_JSON_VALUE)
public class PropertyViewController {

    private final PropertyViewService propertyViewService;

    public PropertyViewController(PropertyViewService propertyViewService) {
        this.propertyViewService = propertyViewService;
    }

    @GetMapping("/list")
    @AccessUserRoles(permitAll = true)
    public ResponseEntity<List<PropertyView>> getViewProperties(@RequestParam(required = false) String lang) {
        return ResponseEntity.ok(propertyViewService.getViewProperties(lang));
    }

    @GetMapping("/list/{propId}")
    @AccessUserRoles(permitAll = true)
    public ResponseEntity<PropertyView> getViewProperty(@PathVariable Long propId,
                                                        @RequestParam(required = false) String lang) {
        return ResponseEntity.ok(propertyViewService.getViewProperty(propId, lang));
    }

    @GetMapping(value = "/list", params = {"cc"})
    @AccessUserRoles(permitAll = true)
    public ResponseEntity<List<PropertyView>> queryViewProperties(@RequestParam(required = false) String lang,
                                                                  @RequestParam String cc,
                                                                  @RequestParam(name = "pcat_type", required = false) CategoryTypes pCatType) {
        return ResponseEntity.ok(propertyViewService.queryViewProperties(lang, cc, pCatType));
    }
}
