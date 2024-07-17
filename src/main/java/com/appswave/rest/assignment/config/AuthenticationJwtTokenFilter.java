package com.appswave.rest.assignment.config;

import com.appswave.rest.assignment.entity.JwtBlacklist;
import com.appswave.rest.assignment.repository.JwtBlacklistRepository;
import com.appswave.rest.assignment.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component

public class AuthenticationJwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserService userService ;

    @Autowired
    private JwtBlacklistRepository jwtBlacklistRepository ;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtTokenUtil.extractUsername(jwt);
            } catch (IllegalArgumentException | ExpiredJwtException | SignatureException |
                    MalformedJwtException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                filterChain.doFilter(request, response);
            }
        }


        if (username == null){
            filterChain.doFilter(request, response);
              return;
        }







        if (jwtBlacklistRepository.findByJwt(jwt) != null){


            filterChain.doFilter(request, response);

            return;
        }


        JwtUserDetails jwtUserDetails  = null ;


        try {
            jwtUserDetails=   userService.loadUserByUsername(username.trim());

        }catch (UsernameNotFoundException e){


            filterChain.doFilter(request, response);

            return;
        }



        if (jwtUserDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {


            if (jwtTokenUtil.validateToken(jwt, jwtUserDetails )) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        jwtUserDetails, null, jwtUserDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            }
        }
        filterChain.doFilter(request, response);



    }
}
