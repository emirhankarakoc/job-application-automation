package com.karakoc.scraper.account;

import com.karakoc.scraper.account.requests.LoginResponse;
import com.karakoc.scraper.user.UserService;
import com.karakoc.scraper.exceptions.general.ForbiddenException;
import com.karakoc.scraper.user.UserDTO;
import com.karakoc.scraper.security.TokenManager;
import com.karakoc.scraper.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
@Service

@RequiredArgsConstructor
public class AuthManager implements AuthService{
    private final TokenManager tokenManager;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public LoginResponse attemptLogin(String email, String password) {
       try{
           var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
           SecurityContextHolder.getContext().setAuthentication(authentication);

           var principal = (UserPrincipal) authentication.getPrincipal();
           var roles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

           var token = tokenManager.issue(principal.getUserId(), principal.getEmail(), roles);
           return LoginResponse.builder()
                   .accessToken(token)
                   .build();
       }
       catch (Exception e){
           throw new ForbiddenException("Wrong email or password");
           //i found the error, in 24. row but i cant fixed. so i found this solution. thanks.
       }
    }


    public UserDTO attemptRegister(String email, String password){
        return userService.createUser(email,password);
    }
}
