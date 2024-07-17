package com.appswave.rest.assignment.service;

import com.appswave.rest.assignment.config.ErrorResponse;
import com.appswave.rest.assignment.config.JwtUserDetails;
import com.appswave.rest.assignment.config.SuccessResponse;
import com.appswave.rest.assignment.dto.UserDTO;
import com.appswave.rest.assignment.entity.JwtBlacklist;
import com.appswave.rest.assignment.entity.User;
import com.appswave.rest.assignment.enums.Role;
import com.appswave.rest.assignment.repository.JwtBlacklistRepository;
import com.appswave.rest.assignment.repository.NewsRepository;
import com.appswave.rest.assignment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtBlacklistRepository jwtBlacklistRepository;
    private final NewsRepository newsRepository ;

    @Autowired
    public UserService(UserRepository userRepository, JwtBlacklistRepository jwtBlacklistRepository, NewsRepository newsRepository) {
        this.userRepository = userRepository;
        this.jwtBlacklistRepository = jwtBlacklistRepository;
        this.newsRepository = newsRepository;
    }

    @Override
    public JwtUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIsIgnoreCase(email);
        if (user == null) {
            throw new UsernameNotFoundException("Email Not Found: " + email);
        }
        return new JwtUserDetails(user);
    }

    public ResponseEntity<?> signup(User user) {
        List<String> errors = validateUser(user,true);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }

        if (userRepository.findByEmailIsIgnoreCase(user.getEmail()) != null) {
            errors.add("Account already exists");
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }

        user.setId(0L);
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user = userRepository.save(user);

        return ResponseEntity.ok(new SuccessResponse("User " + user.getEmail() + " registered successfully!"));
    }

    private List<String> validateUser(User user , boolean withPassword) {
        List<String> errors = new ArrayList<>();
        if (user.getFullName() == null || user.getFullName().trim().length() < 2) {
            errors.add("Full Name is required and must be at least 2 characters long");
        }
        if (user.getEmail() == null || !user.getEmail().trim().matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")) {
            errors.add("Email is not valid");
        }
        if (withPassword) {
            if (user.getPassword() == null || user.getPassword().trim().length() < 8 ||
                    !user.getPassword().trim().matches("^(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$")) {
                errors.add("Password must be at least 8 characters long and contain at least one special character");
            }
        }
        if (user.getDateOfBirth() == null) {
            errors.add("Date Of Birth is required");
        }
        if (user.getRole() == null || !isValidRole(user.getRole())) {
            errors.add("Role is not valid");
        }
        return errors;
    }

    private boolean isValidRole(Role role) {
        return role != null && EnumSet.allOf(Role.class).contains(role);
    }

    public ResponseEntity<?> logout(String jwtToken) {
        if (jwtBlacklistRepository.findByJwt(jwtToken) == null) {
            JwtBlacklist jwtBlacklist = new JwtBlacklist();
            jwtBlacklist.setId(0L);
            jwtBlacklist.setJwt(jwtToken);
            jwtBlacklist.setDate(LocalDate.now());
            jwtBlacklistRepository.save(jwtBlacklist);
        }
        return ResponseEntity.ok(new SuccessResponse("Logged out successfully"));
    }

    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAllUsers(pageable);
    }

    public ResponseEntity<?> findById(Long id) {
        UserDTO userDTO = userRepository.findUserById(id);
        if (userDTO == null){

          return   ResponseEntity.status(404).body(new ErrorResponse("User not found"));
        }


        return ResponseEntity.ok(userDTO);
    }




    public ResponseEntity<?> updateUser(Long userId, User user) {
        return userRepository.findById(userId)
                .map(existingUser -> {
                    List<String> errors = validateUser(user,false);
                    if (!errors.isEmpty()) {
                        return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
                    }
                    existingUser.setEmail(user.getEmail());
                    existingUser.setRole(user.getRole());
                    existingUser.setFullName(user.getFullName());
                    existingUser.setDateOfBirth(user.getDateOfBirth());
                    userRepository.save(existingUser);
                    return ResponseEntity.ok(userRepository.findUserById(existingUser.getId()));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(new ErrorResponse("User not found")));
    }

    public ResponseEntity<?> delete(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()){

            return   ResponseEntity.status(404).body(new ErrorResponse("User not found"));
        }


        newsRepository.deleteByUser(optionalUser.get());
        userRepository.delete(optionalUser.get());

        return ResponseEntity.ok(new SuccessResponse("User deleted successfully!"));


    }
}
