package com.viamatica.pruebatinoreynacrudspring.security.service;

import com.viamatica.pruebatinoreynacrudspring.security.dto.AuthenticationRequest;
import com.viamatica.pruebatinoreynacrudspring.security.dto.AuthenticationResponse;
import com.viamatica.pruebatinoreynacrudspring.security.dto.RegistrationRequest;
import com.viamatica.pruebatinoreynacrudspring.security.entity.User;
import com.viamatica.pruebatinoreynacrudspring.security.repository.UserRepository;
import com.viamatica.pruebatinoreynacrudspring.security.util.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;

    private String crearEmailDesdeNombreUsuario(User user) {
        String[] fullLastName = user.getLastName().toLowerCase().split(" ");
        String emailLastName = "";
        if(fullLastName.length > 1){
            emailLastName = fullLastName[0] + fullLastName[1].charAt(0);
        }
        else emailLastName = user.getLastName().toLowerCase();
        String email = user.getName().toLowerCase().charAt(0) + emailLastName + "@mail.com";
        if(userRepository.findByEmail(email).isPresent()){
            email = user.getName().toLowerCase().charAt(0) + emailLastName + 1 + "@mail.com";
        }
        return email;
    }

    public AuthenticationResponse login(AuthenticationRequest authRequest) {

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword()
        );

        authenticationManager.authenticate(authToken);

        User user = userRepository.findByUsername(authRequest.getUsername()).get();

        String jwt = jwtService.generateToken(user, generateExtraClaims(user));

        return new AuthenticationResponse(jwt);
    }

    public AuthenticationResponse register(RegistrationRequest registrationRequest){
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(registrationRequest.getPassword());
        user.setAccessId(registrationRequest.getAccessId());
        user.setName(registrationRequest.getName());
        user.setLastName(registrationRequest.getLastName());
        String email = crearEmailDesdeNombreUsuario(user);
        //user.setEmail(registrationRequest.getEmail());
        user.setEmail(email);
        if(registrationRequest.getRole() == null)
            user.setRole(Role.USER);
        else
            user.setRole(registrationRequest.getRole());
        userRepository.save(user);
        String jwt = jwtService.generateToken(user, generateExtraClaims(user));
        return new AuthenticationResponse(jwt);
    }

    private Map<String, Object> generateExtraClaims(User user) {

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId()); //
        extraClaims.put("name", user.getName());
        extraClaims.put("accessId", user.getAccessId()); //
        extraClaims.put("lastName", user.getLastName()); //
        extraClaims.put("email", user.getEmail()); //
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("permissions", user.getAuthorities());

        return extraClaims;
    }
}
