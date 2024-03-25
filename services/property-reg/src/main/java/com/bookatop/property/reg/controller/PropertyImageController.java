package com.bookatop.property.reg.controller;

import com.bookatop.property.reg.model.PropertyImage;
import com.bookatop.property.reg.model.PropertyResponse;
import com.bookatop.property.reg.service.ImageStorageService;
import com.bookatop.security.annotation.AccessUserRoles;
import com.bookatop.security.enums.UserRoles;
import com.bookatop.security.request.GatewayHeaders;
import com.sun.jersey.core.util.Base64;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping(value = "property-reg/images", produces = MediaType.APPLICATION_JSON_VALUE)
public class PropertyImageController {

    public static final String IMAGE_IS_REMOVED = "Image is removed";

    private final ImageStorageService imageStorageService;

    public PropertyImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PostMapping(value = "/add", consumes = MediaType.IMAGE_JPEG_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessUserRoles(UserRoles.USER)
    public ResponseEntity<PropertyImage> addImages(@RequestHeader(GatewayHeaders.UID) Long userId,
                                                   HttpServletRequest httpServletRequest) throws IOException {
        String fPath = imageStorageService.storeImage(userId, httpServletRequest.getInputStream());
        return ResponseEntity.ok(new PropertyImage(fPath));
    }

    /* The /remove end-point accepts file parameter encoded base64 */
    @DeleteMapping("/remove")
    @AccessUserRoles(UserRoles.USER)
    public ResponseEntity<PropertyResponse> removeImage(@RequestHeader(GatewayHeaders.UID) Long userId,
                                                        @RequestParam String file) {
        imageStorageService.removeImage(userId, Base64.base64Decode(file));
        return ResponseEntity.ok(new PropertyResponse(IMAGE_IS_REMOVED));
    }
}
