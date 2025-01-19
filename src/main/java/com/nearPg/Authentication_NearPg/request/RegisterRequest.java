package com.nearPg.Authentication_NearPg.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;

}
