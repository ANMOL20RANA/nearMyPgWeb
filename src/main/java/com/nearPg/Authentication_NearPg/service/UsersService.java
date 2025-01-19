package com.nearPg.Authentication_NearPg.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.nearPg.Authentication_NearPg.request.LoginRequest;
import com.nearPg.Authentication_NearPg.request.RegisterRequest;
import com.nearPg.Authentication_NearPg.responses.LoginResponse;
import com.nearPg.Authentication_NearPg.responses.RegisterResponse;

public interface UsersService {

    RegisterResponse register(RegisterRequest registerRequest);

    String generateOTP();

    void sendEmailVerification(String email, String otp);

    void verify(String email, String otp);

    LoginResponse login(LoginRequest LoginRequest);

    LoginResponse verifyLoginOTP(@RequestParam String email, @RequestParam String otp);

    String resendVerificationOtp(String email);

    String resendLoginOtp(String email);

}
