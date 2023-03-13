package it.epicode.forum.controller;

import it.epicode.forum.config.JwtUtils;
import it.epicode.forum.config.UserDetailsImpl;
import it.epicode.forum.entity.Role;
import it.epicode.forum.entity.User;
import it.epicode.forum.login.LoginRequest;
import it.epicode.forum.login.LoginResponse;
import it.epicode.forum.repo.RoleRepo;
import it.epicode.forum.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class AuthController {

    @Autowired(required = true)
    AuthenticationManager authManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordEncoder pe;

    @Autowired
    RoleRepo rp;

    @Autowired
    UserRepo ur;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = jwtUtils.generateJwtToken(auth);

        UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();

        List<String> roles = user.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList());

        return ResponseEntity.ok(new LoginResponse(jwt,user.getUsername(), roles, user.getExpirationTime()));
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<User> signUp(@RequestBody User user) {
        List<Role> roles = new ArrayList<>();
        Role role = new Role();
        role.setName("ROLE_USER");
        rp.save(role);
        roles.add(role);
        
        user.setRoleList(roles);
        user.setActive(true);
        user.setDataRegistrazione(LocalDate.now());
        user.setPassword(pe.encode(user.getPassword()));
        User u = ur.save(user);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    public List<User> getAllUsers() {
        return ur.findAll();
    }

    @GetMapping("/user2")
    public List<User> getAllUsers2() {
        return ur.findAll();
    }

    @GetMapping("/profile")
    public User getLoggedUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return ur.findByUsername(currentPrincipalName);
    }

    @GetMapping("/user/{id}")
    public Optional<User> getByPostById(@RequestParam int id) {
        return ur.findById(id);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/ciao")
    public String ciao() {
        return "Ciao World";
    }


}
