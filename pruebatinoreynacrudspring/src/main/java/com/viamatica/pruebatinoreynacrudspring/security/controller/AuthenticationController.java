package com.viamatica.pruebatinoreynacrudspring.security.controller;

import com.viamatica.pruebatinoreynacrudspring.security.dto.AuthenticationRequest;
import com.viamatica.pruebatinoreynacrudspring.security.dto.AuthenticationResponse;
import com.viamatica.pruebatinoreynacrudspring.security.dto.RegistrationRequest;
import com.viamatica.pruebatinoreynacrudspring.security.entity.Session;
import com.viamatica.pruebatinoreynacrudspring.security.entity.User;
import com.viamatica.pruebatinoreynacrudspring.security.repository.SessionRepository;
import com.viamatica.pruebatinoreynacrudspring.security.repository.UserRepository;
import com.viamatica.pruebatinoreynacrudspring.security.service.AuthenticationService;
import com.viamatica.pruebatinoreynacrudspring.security.util.JsonSchemaValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {


    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private JsonSchemaValidatorUtil jsonSchemaValidatorUtil;

    private void encriptarClaveUsuario(RegistrationRequest registrationRequest) {
        String claveEncriptada = passwordEncoder.encode(registrationRequest.getPassword());
        registrationRequest.setPassword(claveEncriptada);
    }

    private void creaSesion(AuthenticationRequest authRequest) {
        Session session = new Session();
        User user = userRepository.findByUsername(authRequest.getUsername()).get();
        session.setUser(user);
        session.setFechaInicioSesion(new Date());
        sessionRepository.save(session);
    }

    @PreAuthorize("permitAll")
    @PostMapping("/authenticate")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authRequest){
        Map<String, Object> response = new HashMap<>();

        try{
            AuthenticationResponse jwtDto = authenticationService.login(authRequest);
            creaSesion(authRequest);
            response.put("mensaje", "Se ha dado acceso al usuario");
            response.put("jwt", jwtDto.getJwt());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (DataAccessException e) {
            response.put("mensaje", "Error al registrar el usuario en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("permitAll")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registrationRequest){
        Map<String, Object> response = new HashMap<>();
        String jsonRequest = jsonSchemaValidatorUtil.convertObjectToJson(registrationRequest);
        System.out.println(jsonRequest);
        if (!jsonSchemaValidatorUtil.validateJson(jsonRequest, "registration-request-schema.json")) {
            response.put("mensaje", "El JSON no cumple con el esquema de validación");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(userRepository.findByUsername(registrationRequest.getUsername()).isPresent()){
            response.put("mensaje", "El nombre de usuario " + registrationRequest.getUsername() +" ya está en uso");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        try{
            encriptarClaveUsuario(registrationRequest);
            AuthenticationResponse jwtDto = authenticationService.register(registrationRequest);
            response.put("mensaje", "El usuario ha sido registrado con éxito");
            response.put("jwt", jwtDto.getJwt());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }catch (DataAccessException e) {
            response.put("mensaje", "Error al registrar el usuario en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("permitAll")
    @PatchMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RegistrationRequest registrationRequest){
        return null;
    }

    @PreAuthorize("permitAll")
    @GetMapping("/public-access")
    public String publicAccessEndpoint(){
        return "Este endpoint es público";
    }

}
