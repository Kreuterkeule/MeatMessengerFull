package com.kreuterkeule.MeatMessenger.controller;

import com.kreuterkeule.MeatMessenger.dto.AddContactDto;
import com.kreuterkeule.MeatMessenger.dto.AuthResponseDto;
import com.kreuterkeule.MeatMessenger.dto.LoginDto;
import com.kreuterkeule.MeatMessenger.dto.RegisterDto;
import com.kreuterkeule.MeatMessenger.models.RoleEntity;
import com.kreuterkeule.MeatMessenger.models.UserEntity;
import com.kreuterkeule.MeatMessenger.repositories.RoleRepository;
import com.kreuterkeule.MeatMessenger.repositories.UserRepository;
import com.kreuterkeule.MeatMessenger.security.JwtUtils;
import com.kreuterkeule.MeatMessenger.services.UniqueTokenProviderService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private AuthenticationManager authenticationManager;

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private JwtUtils jwtUtils;

    private UniqueTokenProviderService uniqueTokenProviderService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, UniqueTokenProviderService uniqueTokenProviderService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.uniqueTokenProviderService = uniqueTokenProviderService;
    }

    @PostMapping(path = "login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtils.generateToken(authentication, request);

        return new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK);
    }

    @PostMapping("logout")
    public ResponseEntity<AuthResponseDto> logout(HttpServletRequest request) {

        return new ResponseEntity<>(new AuthResponseDto(""), HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return new ResponseEntity<>("Username already taken!", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setIdentifierToken(uniqueTokenProviderService.generateToken()); // TODO: make unique token really unique

        RoleEntity roles = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(roles));

        userRepository.save(user);

        return new ResponseEntity<>("User registered", HttpStatus.OK);
    }

    @GetMapping("delete")
    public ResponseEntity<UserEntity> delete(@RequestParam("id") int id, HttpServletRequest request, Model model) {

        String token = request.getHeader("Authorization").substring(7);
        UserEntity user = userRepository.findByUsername(jwtUtils.getUsernameFromJwt(token)).get();
        if (user.getId() == id) {
            user.setRoles(new ArrayList<>());
            userRepository.deleteById(id);

            return ResponseEntity.ok(user);
        }
        System.out.println("User '" + user.getUsername() + "' is trying to delete user with id '" + id + "' but isn't the user, so he isn't permitted");
        UserEntity badRequestUser = new UserEntity();
        badRequestUser.setUsername("BadRequest");
        badRequestUser.setPassword("BadRequest");
        return ResponseEntity.ok(badRequestUser);
    }

    @GetMapping("getUserInfo")
    public ResponseEntity<UserEntity> getUserInfo(HttpServletRequest request) {
        return new ResponseEntity<>(
                userRepository.findByUsername(
                        jwtUtils.getUsernameFromJwt(
                                request.getHeader(
                                        "Authorization"
                                ).substring(
                                        7
                                )
                        )
                ).get(),
                HttpStatus.OK);
    }

    @GetMapping("getContacts")
    public ResponseEntity<Map<String, String>> getContacts(HttpServletRequest request) {
        UserEntity client = userRepository.findByUsername(jwtUtils.getUsernameFromJwt(jwtUtils.getJWTFromRequest(request))).get();
        if (client == null) {
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(client.getContacts(), HttpStatus.OK);
    }

    @PostMapping("addContact")
    public ResponseEntity<Map<String, String>> addContact(@RequestBody AddContactDto body, HttpServletRequest request) {
        UserEntity client = userRepository.findByUsername(jwtUtils.getUsernameFromJwt(jwtUtils.getJWTFromRequest(request))).get();
        if (body.getNickname() == "") {
            UserEntity contact = userRepository.findByIdentifierToken(body.getToken()).get();
            if (contact != null) {
                body.setNickname(contact.getUsername());
                client.addToContacts(body.getToken(), body.getNickname());
                userRepository.save(client);
                return new ResponseEntity<>(client.getContacts(), HttpStatus.OK);
            }
        } else {
            UserEntity contact = userRepository.findByIdentifierToken(body.getToken()).get();
            if (contact != null) {
                client.addToContacts(body.getToken(), body.getNickname());
                userRepository.save(client);
                return new ResponseEntity<>(client.getContacts(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(client.getContacts(), HttpStatus.OK);
    }

}
