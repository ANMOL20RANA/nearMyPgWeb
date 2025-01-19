package com.nearPg.Authentication_NearPg.service.IMPL;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.nearPg.Authentication_NearPg.JwtSupport_TO_0TP.Config.JwtService;
import com.nearPg.Authentication_NearPg.model.Role;
import com.nearPg.Authentication_NearPg.model.Users;
import com.nearPg.Authentication_NearPg.repository.RoleRepository;
import com.nearPg.Authentication_NearPg.repository.UsersRepository;
import com.nearPg.Authentication_NearPg.request.LoginRequest;
import com.nearPg.Authentication_NearPg.request.RegisterRequest;
import com.nearPg.Authentication_NearPg.responses.LoginResponse;
import com.nearPg.Authentication_NearPg.responses.RegisterResponse;
import com.nearPg.Authentication_NearPg.service.UsersService;

@Service
public class UsersServiceImple implements UsersService {

    private final UsersRepository usersRepository;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersServiceImple(UsersRepository usersRepository, EmailService emailService, RoleRepository roleRepository,
            JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${otp.expiration.time}")
    private int plusMinutes;

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        Optional<Users> dbUser = usersRepository.findByEmail(registerRequest.getEmail());

        Users existingUser = null;
        if (dbUser.isPresent()) {
            existingUser = dbUser.get();
        }

        if (existingUser != null && existingUser.isVerified()) {
            throw new RuntimeException("User Already Registered");

        }
        // Fetch the Role
        Role userRole = roleRepository.findByRoleName("USER");
        if (userRole == null) {
            throw new RuntimeException("Role 'USER' not found");
        }

        Users users = Users.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest
                        .getPassword()))
                .role(userRole)
                .build();

        String otp = generateOTP();
        users.setOtp(otp);
        users.setOtpExpiration(LocalDateTime.now().plusMinutes(plusMinutes));
        Users savedUser = usersRepository.save(users);
        sendEmailVerification(savedUser.getEmail(), otp);
        RegisterResponse response = RegisterResponse.builder()
                .firstName(users.getFirstName())
                .lastName(users.getLastName())
                .email(users.getEmail()).build();

        return response;

    }

    @Override
    public String generateOTP() {
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        return String.valueOf(otpValue);
    }

    @Override
    public void sendEmailVerification(String email, String otp) {
        String subject = " Email Verification";
        String body = "Your verification otp is :" + otp;
        emailService.sendEmail(email, subject, body);

    }

    @Override
    public void verify(String email, String otp) {
        Optional<Users> dbUser = usersRepository.findByEmail(email);

        Users existingUser = null;
        if (dbUser.isPresent()) {
            existingUser = dbUser.get();
        }
        if (existingUser == null) {
            throw new RuntimeException("User not Found");
        } else if (existingUser.isVerified()) {
            throw new RuntimeException("User is already verified");

        } else if (otp.equals(existingUser.getOtp())) {
            if (existingUser.getOtpExpiration().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("OTP has expired");
            }
            existingUser.setVerified(true);
            usersRepository.save(existingUser);

        } else {
            throw new RuntimeException("Internal Server Error");
        }

    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // Step 1: Find the user by email
        Optional<Users> dbUser = usersRepository.findByEmail(loginRequest.getEmail());

        Users existingUser = null;
        if (dbUser.isPresent()) {
            existingUser = dbUser.get();
        }

        // Check if the user exists
        if (existingUser == null) {
            throw new RuntimeException("User does not exist");
        }

        // Step 2: Check if the user is verified
        if (!existingUser.isVerified()) {
            throw new RuntimeException("User is not verified");
        }

        // Step 3: Generate and save OTP
        String otp = generateOTP();
        existingUser.setLoginOtp(otp); // Assuming there's a field for OTP in the user entity
        existingUser.setLoginOtpExpiration(LocalDateTime.now().plusMinutes(plusMinutes));
        usersRepository.save(existingUser); // Save the OTP in the database

        // Step 4: Send OTP to user's email
        sendEmailVerification(existingUser.getEmail(), otp);

        // Step 5: Return login response (before OTP verification)
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setMessage(
                "Hello " + existingUser.getEmail() + ", an OTP has been sent to your email for verification.");

        return loginResponse;
    }

    @Override
    public LoginResponse verifyLoginOTP(@RequestParam String email, @RequestParam String otp) {
        // Step 1: Find the user by email
        Optional<Users> dbUser = usersRepository.findByEmail(email);

        Users existingUser = null;
        if (dbUser.isPresent()) {
            existingUser = dbUser.get();
        }
        // Step 2: Check if the user exists
        if (existingUser == null) {
            throw new RuntimeException("User does not exist");
        }

        // Step 3: Check if the OTP matches
        if (!(otp.equals(existingUser.getLoginOtp()))) {
            throw new RuntimeException("Invalid OTP");
        }
        // Step 4: Check if the OTP has expired

        if (existingUser.getLoginOtpExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Login OTP has expired");
        }

        // Step 4: Clear OTP (once used, you can clear the OTP field)
        existingUser.setLoginOtp(null);
        usersRepository.save(existingUser);

        // Step 6: Generate JWT token
        String jwtToken = jwtService.generateToken(existingUser);

        // Step 5: Return successful login response
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setMessage("Hello " + existingUser.getEmail() + ", you have successfully logged in.");
        loginResponse.setJwtToken(jwtToken); // Add the JWT token to the response

        return loginResponse;
    }

    @Override
    public String resendVerificationOtp(String email) {
        Optional<Users> dbUser = usersRepository.findByEmail(email);

        Users existingUser = null;
        if (dbUser.isPresent()) {
            existingUser = dbUser.get();
        }
        // Check if the user exists
        if (existingUser == null) {
            throw new RuntimeException("User does not exist");
        }

        // Generate a new OTP
        String otp = generateOTP();

        // Set the new OTP and expiration time (5 minutes from now)
        existingUser.setOtp(otp);
        existingUser.setOtpExpiration(LocalDateTime.now().plusMinutes(plusMinutes)); // OTP expires in 5 minutes

        usersRepository.save(existingUser); // Save the updated OTP and expiration time

        // Send the new OTP to the user's email
        sendEmailVerification(existingUser.getEmail(), otp);

        // Return confirmation message
        return "A new OTP has been sent to your email " + existingUser.getEmail() + " for verification.";
    }

    @Override
    public String resendLoginOtp(String email) {
        // Find the user by email
        Optional<Users> dbUser = usersRepository.findByEmail(email);

        Users existingUser = null;
        if (dbUser.isPresent()) {
            existingUser = dbUser.get();
        }
        // Check if the user exists
        if (existingUser == null) {
            throw new RuntimeException("User does not exist");
        }

        // Check if the user is verified
        if (!existingUser.isVerified()) {
            throw new RuntimeException("User is not verified");
        }

        // Generate a new OTP for login
        String otp = generateOTP();

        // Set the new OTP and expiration time (5 minutes from now)
        existingUser.setLoginOtp(otp);
        existingUser.setLoginOtpExpiration(LocalDateTime.now().plusMinutes(plusMinutes)); // OTP expires in 5 minutes
        usersRepository.save(existingUser); // Save the updated OTP and expiration time

        // Send the new OTP to the user's email
        sendEmailVerification(existingUser.getEmail(), otp);

        // Return confirmation message
        return "A new login OTP has been sent to your email " + existingUser.getEmail() + ".";
    }

}
