package com.appswave.rest.assignment.controller;


import com.appswave.rest.assignment.config.ErrorResponse;
import com.appswave.rest.assignment.dto.UserDTO;
import com.appswave.rest.assignment.entity.User;
import com.appswave.rest.assignment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {


    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<UserDTO> getAllUsers(@RequestParam(defaultValue = "0") int pageNumber,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(defaultValue = "id") String sortBy,
                                     @RequestParam(defaultValue = "asc") String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userService.delete(id);
    }


    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("An error occurred", List.of(ex.getMessage()));

        if (ex.getMessage().contains("appswave.rest.assignment.dto.LoginRequest")){
            return null;

        }

        if (ex.getMessage().contains("Duplicate entry")){

            errorResponse = new ErrorResponse("Account already exists");
            return ResponseEntity.badRequest().body(errorResponse);

        }
        if (ex.getMessage().contains("[CONTENT_WRITER, ADMIN, NORMAL]")){
            errorResponse = new ErrorResponse("Validation failed", List.of("Role is not valid. It should be one of the following:[CONTENT_WRITER, ADMIN, NORMAL] "));
            return ResponseEntity.badRequest().body(errorResponse);


        }
        return ResponseEntity.status(500).body(errorResponse);
    }


}
