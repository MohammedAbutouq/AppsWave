package com.appswave.rest.assignment.helper;

import com.appswave.rest.assignment.config.JwtUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Helper {


    public  JwtUserDetails getUserDetailsFromToken (){


        try {


        JwtUserDetails jwtUserDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        return jwtUserDetails;
        } catch (Exception e){

            return  null ;

        }

    }


}