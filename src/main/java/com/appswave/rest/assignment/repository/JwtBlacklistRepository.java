package com.appswave.rest.assignment.repository;

import com.appswave.rest.assignment.entity.JwtBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {
    JwtBlacklist findByJwt(String jwt);


}