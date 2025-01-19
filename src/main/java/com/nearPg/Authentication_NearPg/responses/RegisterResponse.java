package com.nearPg.Authentication_NearPg.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterResponse {

    private String firstName;
    private String lastName;
    private String email;

}
