package com.nearPg.Authentication_NearPg.AuthenticationController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nearPg.Authentication_NearPg.request.LoginRequest;
import com.nearPg.Authentication_NearPg.request.RegisterRequest;
import com.nearPg.Authentication_NearPg.responses.LoginResponse;
import com.nearPg.Authentication_NearPg.responses.RegisterResponse;
import com.nearPg.Authentication_NearPg.service.UsersService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsersService usersService;

    @Autowired
    public AuthController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = usersService.register(registerRequest);
        return new ResponseEntity<>(registerResponse, HttpStatus.CREATED);
    }

    @PostMapping("/verified")
    public ResponseEntity<?> verifyUser(@RequestParam String email, @RequestParam String otp) {
        try {
            usersService.verify(email, otp);
            return new ResponseEntity<>("User verified Successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Call the login method from service and return the response
            LoginResponse loginResponse = usersService.login(loginRequest);
            return new ResponseEntity<>(loginResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            // Return error response if something goes wrong
            return new ResponseEntity<>(new LoginResponse(ex.getMessage(), "Jwt Token Failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/verify-Login-otp")
    public ResponseEntity<LoginResponse> verifyLoginOTP(@RequestParam String email, @RequestParam String otp) {
        try {
            // Call the OTP verification method from service and return the response
            LoginResponse loginResponse = usersService.verifyLoginOTP(email, otp);
            return new ResponseEntity<>(loginResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            // Return error response if something goes wrong
            return new ResponseEntity<>(new LoginResponse(ex.getMessage(), "Jwt Token Failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/resend-verification-otp")
    public ResponseEntity<String> resendVerificationOtp(@RequestParam String email) {
        try {
            String responseMessage = usersService.resendVerificationOtp(email);
            return ResponseEntity.ok(responseMessage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Resend OTP for login
    @PostMapping("/resend-login-otp")
    public ResponseEntity<String> resendLoginOtp(@RequestParam String email) {
        try {
            String responseMessage = usersService.resendLoginOtp(email);
            return ResponseEntity.ok(responseMessage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/hello")
    public String securedPoint() {
        return "Hello from Secured Point";

    }

}
