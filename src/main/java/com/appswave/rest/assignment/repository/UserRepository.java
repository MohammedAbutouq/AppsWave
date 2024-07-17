package com.appswave.rest.assignment.repository;

import com.appswave.rest.assignment.dto.UserDTO;
import com.appswave.rest.assignment.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmailIsIgnoreCase(String email);



    @Query("select u.id as id, u.fullName as fullName, u.email as email, u.dateOfBirth as dateOfBirth, u.role as role from User u")
    Page<UserDTO> findAllUsers(Pageable pageable);

    @Query("select u.id as id, u.fullName as fullName, u.email as email, u.dateOfBirth as dateOfBirth, u.role as role from User u where u.id = ?1")
    UserDTO findUserById(Long userId);








}









