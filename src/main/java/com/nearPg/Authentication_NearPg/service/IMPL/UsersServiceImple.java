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
import com.nearPg.Authentication_NearPg.model.TemraryOtp;
import com.nearPg.Authentication_NearPg.model.Users;
import com.nearPg.Authentication_NearPg.repository.RoleRepository;
import com.nearPg.Authentication_NearPg.repository.TemprarayOtpRepository;
import com.nearPg.Authentication_NearPg.repository.UsersRepository;
import com.nearPg.Authentication_NearPg.request.LoginRequest;
import com.nearPg.Authentication_NearPg.request.RegisterRequest;
import com.nearPg.Authentication_NearPg.responses.LoginResponse;
import com.nearPg.Authentication_NearPg.responses.RegisterResponse;
import com.nearPg.Authentication_NearPg.service.UsersService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsersServiceImple implements UsersService {

    private final UsersRepository usersRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final TemprarayOtpRepository temprarayOtpRepository;
    private final RoleRepository roleRepository;

    @Value("${otp.expiration.time}")
    private int plusMinutes;

    public Users register(String email) {
        Optional<Users> dbUser = usersRepository.findByEmail(email);

        Users existingUser = null;
        if (dbUser.isPresent()) {
            existingUser = dbUser.get();
        }

        if (existingUser != null) {
            throw new RuntimeException("User Already Registered");

        }
        // Fetch the Role
        Role userRole = roleRepository.findByRoleName("USER");
        if (userRole == null) {
            throw new RuntimeException("Role 'USER' not found");
        }

        Users users = Users.builder()
                .email(email)
                .role(userRole)
                .build();
        return usersRepository.save(users);

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
    public LoginResponse login(LoginRequest loginRequest) {
        // Step 1: Find the user by email
        Optional<Users> dbUser = usersRepository.findByEmail(loginRequest.getEmail());

        // Check if the user exists
        if (!dbUser.isPresent()) {
            // Users user = new Users();
            TemraryOtp tepUser = new TemraryOtp();
            String otp = generateOTP();
            tepUser.setEmail(loginRequest.getEmail());
            tepUser.setOtp(otp);
            temprarayOtpRepository.save(tepUser);
            sendEmailVerification(loginRequest.getEmail(), otp);
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setMessage(
                    "Hello " + loginRequest.getEmail() + ", an OTP has been sent to your email for verification.");

            return loginResponse;
        } else {

            Users existingUser = null;
            if (dbUser.isPresent()) {
                existingUser = dbUser.get();
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
    }

    @Override
    public LoginResponse verifyLoginOTP(@RequestParam String email, @RequestParam String otp) {
        // Step 1: Find the user by email
        Optional<Users> dbUser = usersRepository.findByEmail(email);

        if (!dbUser.isPresent()) {

            // check in temprary table
            Optional<TemraryOtp> tepUser = temprarayOtpRepository.findByEmail(email);
            // If its not exist in temprary table
            if (!tepUser.isPresent()) {
                throw new RuntimeException("Email not exsist");
            }
            // If its present in Temprary table
            TemraryOtp existTemraryuser = null;
            if (tepUser.isPresent()) {
                existTemraryuser = tepUser.get();
            }
            // compare the otp
            if (!(otp.equals(existTemraryuser.getOtp()))) {
                throw new RuntimeException("Invalid OTP");
            }

            // save the user
            Users existingUser = register(existTemraryuser.getEmail());
            // Otp verified clear the data from temrary table
            temprarayOtpRepository.deleteById(existTemraryuser.getId());

            // Step 6: Generate JWT token
            String jwtToken = jwtService.generateToken(existingUser);

            // Step 5: Return successful login response
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setMessage("Hello " + existingUser.getEmail() + ", you have successfully logged in.");
            loginResponse.setJwtToken(jwtToken); // Add the JWT token to the response

            return loginResponse;

        } else {

            Users existingUser = null;
            if (dbUser.isPresent()) {
                existingUser = dbUser.get();
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

        // // Check if the user is verified
        // if (!existingUser.isVerified()) {
        // throw new RuntimeException("User is not verified");
        // }

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
