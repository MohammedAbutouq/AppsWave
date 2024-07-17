package com.appswave.rest.assignment.dto;

import com.appswave.rest.assignment.enums.Role;

import java.time.LocalDate;

public interface UserDTO {


    Long getId();
    String getFullName();
    String getEmail();
    LocalDate getDateOfBirth();
    Role getRole();

}
