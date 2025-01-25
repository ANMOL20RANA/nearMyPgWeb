package com.nearPg.Authentication_NearPg.oauth2Setup.oauth2Controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/oauth2/v1/demo")
public class OauthController {

    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hell OAuth2");
    }

    @GetMapping("/user")
    public Principal users(Principal user) {
        return user;
    }

    @GetMapping("/jwt-token")
    public String getJwtToken(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return "User is not authenticated";
        }

        // Get the JWT token from the OAuth2User attributes
        String jwtToken = (String) oAuth2User.getAttributes().get("jwtToken");

        if (jwtToken == null) {
            return "JWT Token not found";
        }

        return jwtToken;
    }

}
