package com.appswave.rest.assignment.controller;


import com.appswave.rest.assignment.config.ErrorResponse;

import com.appswave.rest.assignment.config.JwtTokenUtil;
import com.appswave.rest.assignment.config.JwtUserDetails;
import com.appswave.rest.assignment.dto.JwtResponse;
import com.appswave.rest.assignment.dto.LoginRequest;
import com.appswave.rest.assignment.entity.User;
import com.appswave.rest.assignment.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService ;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil ;





    @PostMapping("/login")
    public  ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws Exception {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword() ;

        JwtUserDetails jwtUserDetails = jwtUserDetails = authenticate(email, password);

        String jwtToken = jwtTokenUtil.generateToken(jwtUserDetails) ;

        return ResponseEntity.ok().body(new JwtResponse( jwtToken));



    }




    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            return userService.logout(token);

        }

            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid JWT token", null));

    }







    private JwtUserDetails authenticate(String email, String password) throws Exception {

        JwtUserDetails   jwtUserDetails = userService.loadUserByUsername(email);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password ,jwtUserDetails.getAuthorities()));



        return jwtUserDetails;

    }


    @RequestMapping( value = "/signup",method = RequestMethod.POST)
    public ResponseEntity<?> signup(@Valid @RequestBody User user  , BindingResult result ) throws Exception {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            ErrorResponse errorResponse = new ErrorResponse("Validation failed", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        return userService.signup(user) ;
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("An error occurred", List.of(ex.getMessage()));

        if (ex.getMessage().contains("appswave.rest.assignment.dto.LoginRequest")){
            return null;

        }
        if (ex.getMessage().contains("[CONTENT_WRITER, ADMIN, NORMAL]")){
            errorResponse = new ErrorResponse("Validation failed", List.of("Role is not valid. It should be one of the following:[CONTENT_WRITER, ADMIN, NORMAL] "));
            return ResponseEntity.badRequest().body(errorResponse);


        }
        return ResponseEntity.status(500).body(errorResponse);
    }




}
