package com.nearPg.Authentication_NearPg.oauth2Setup.SecurityConfig;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.nearPg.Authentication_NearPg.JwtSupport_TO_0TP.Config.JwtService;
import com.nearPg.Authentication_NearPg.model.Role;
import com.nearPg.Authentication_NearPg.model.Users;
import com.nearPg.Authentication_NearPg.repository.RoleRepository;
import com.nearPg.Authentication_NearPg.repository.UsersRepository;
import com.nearPg.Authentication_NearPg.service.IMPL.UsersServiceImple;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UsersRepository userRepository; // Assuming you have a UserRepository
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final UsersServiceImple usersServiceImple;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // Extract user details from the OAuth2 response (e.g., email, name)
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");
        if (email == null) {
            throw new IllegalArgumentException("Email attribute is missing");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name attribute is missing");
        }

        // Debugging logs to check the attributes returned by OAuth2 response
        System.out.println("OAuth2 Response Attributes: " + oAuth2User.getAttributes());
        System.out.println("Email: " + email);
        System.out.println("Name: " + name);

        // Check if the user already exists in your database
        java.util.Optional<Users> userOptional = userRepository.findByEmail(email);

        Users user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            // If the user does not exist, create and save them to the database
            Role userRole = roleRepository.findByRoleName("USER");
            if (userRole == null) {
                throw new RuntimeException("Role 'USER' not found");
            }

            user = new Users();
            user.setEmail(email);
            user.setFirstName((String) oAuth2User.getAttributes().get("name"));
            user.setRole(userRole);
            usersServiceImple.register(email);
        }

        // Generate JWT token for the user
        String jwtToken = jwtService.generateToken(user);
        Map<String, Object> mutableAttributes = new HashMap<>(oAuth2User.getAttributes());
        mutableAttributes.put("jwtToken", jwtToken);

        OAuth2User updatedOAuth2User = new DefaultOAuth2User(
                oAuth2User.getAuthorities(), // Retain the authorities
                mutableAttributes, // Use the updated mutable attributes
                oAuth2User.getName() // Retain the name attribute key
        );

        return updatedOAuth2User;
    }
}
