package com.secure_resource.secure_resource.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class Resource {

    @GetMapping("/resource")
    public String resource(@AuthenticationPrincipal Jwt jwt) {
        log.info("***** JWT Headers: {}", jwt.getHeaders());
        log.info("**** JWT Claims: {}", jwt.getClaims().toString());
        log.info("***** JWT Token: {}", jwt.getTokenValue());
        return String.format("Resource successfully accessed by: %s (with subjectId: %s)" ,
                jwt.getAudience().get(0),
                jwt.getSubject());
    }
}
