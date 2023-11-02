package com.viamatica.pruebatinoreynacrudspring.security.controller;

import com.viamatica.pruebatinoreynacrudspring.security.entity.User;
import com.viamatica.pruebatinoreynacrudspring.security.repository.UserRepository;
import com.viamatica.pruebatinoreynacrudspring.security.util.JsonSchemaValidatorUtil;
import com.viamatica.pruebatinoreynacrudspring.security.util.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // codificador de password
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JsonSchemaValidatorUtil jsonSchemaValidatorUtil;

    private void encriptarClaveUsuario(User usuario) {
        String claveEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(claveEncriptada);
    }


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


    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_USER')")
    @GetMapping("/users")
    /*public ResponseEntity<Page<User>> listarUsuarios (@PageableDefault(page=0, size=5) Pageable pageable){
        return ResponseEntity.ok(userRepository.findAll(pageable));
    }*/
    // devuelve una lista completa o paginada si viajan parámetros de paginación
    public ResponseEntity<Object> listarUsuarios(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            // Si se proporcionan los parámetros de paginación, devuelve una lista paginada
            Pageable pageable = PageRequest.of(page, size);
            Page<User> pageResult = userRepository.findAll(pageable);
            return ResponseEntity.ok(pageResult);
        } else {
            // Si no se proporcionan los parámetros de paginación, devuelve una lista completa
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_USER')")
    @GetMapping("/users/{id}")
    public ResponseEntity<?> buscarUsuario(@PathVariable Long id){
        User usuario = null;
        Map<String, Object> response = new HashMap<>();

        try {
            usuario = userRepository.findById(id).get();
        }catch(DataAccessException e) {
            response.put("mensaje", "Error al realizar la consulta en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<User>(usuario, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @PostMapping("/users")
    public ResponseEntity<?> guardarUsuario(@RequestBody User usuario){
        User usuarioNuevo = null;
        Map<String, Object> response = new HashMap<>();

        // si no viaja el ROL, por defecto debe ser el de USUARIO
        if(usuario.getRole() == null)
            usuario.setRole(Role.USER);

        // proceso de validación
        String jsonRequest = jsonSchemaValidatorUtil.convertObjectToJson(usuario);
        if (!jsonSchemaValidatorUtil.validateJson(jsonRequest, "user-schema.json")) {
            response.put("mensaje", "El JSON no cumple con el esquema de validación");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // encripta clave
        encriptarClaveUsuario(usuario);

        try {
            String email = crearEmailDesdeNombreUsuario(usuario);
            usuario.setEmail(email);
            usuarioNuevo = userRepository.save(usuario);
        } catch(DataAccessException e) {
            response.put("mensaje", "Error al realizar el insert en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("mensaje", "El usuario ha sido creado con éxito");
        response.put("usuario", usuarioNuevo);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @PutMapping("/users/{id}")
    public ResponseEntity<?> editarUsuario(@RequestBody User usuario, @PathVariable Long id){
        User usuarioActual = userRepository.findById(id).get();
        User usuarioEditado = null;
        Map<String, Object> response = new HashMap<>();

        // proceso de validación
        String jsonRequest = jsonSchemaValidatorUtil.convertObjectToJson(usuario);
        if (!jsonSchemaValidatorUtil.validateJson(jsonRequest, "user-schema.json")) {
            response.put("mensaje", "El JSON no cumple con el esquema de validación");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            usuarioActual.setName(usuario.getName());
            usuarioActual.setLastName(usuario.getLastName());
            usuarioActual.setEmail(usuario.getEmail());
            usuarioActual.setUsername(usuario.getUsername());
            // encripta clave
            encriptarClaveUsuario(usuario);
            usuarioActual.setPassword(usuario.getPassword());
            // si no viaja el ROL, por defecto debe ser el de USUARIO
            if(usuario.getRole() == null)
                usuario.setRole(Role.USER);
            else
                usuarioActual.setRole(usuario.getRole());
            usuarioEditado = userRepository.save(usuarioActual);
        } catch(DataAccessException e) {
            response.put("mensaje", "Error al realizar el update en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("mensaje", "El usuario ha sido editado con éxito");
        response.put("usuario", usuarioEditado);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.ACCEPTED);
    }

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> borrarUsuario(@PathVariable Long id){
        Map<String, Object> response = new HashMap<>();

        try {
            userRepository.deleteById(id);
        }catch(DataAccessException e) {
            response.put("mensaje", "Error al realizar el delete en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("mensaje", "El usuario ha sido eliminado con éxito");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.ACCEPTED);
    }
}
