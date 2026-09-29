package com.karakoc.scraper.account;

import com.karakoc.scraper.account.requests.LoginRequest;
import com.karakoc.scraper.account.requests.LoginResponse;
import com.karakoc.scraper.account.requests.RegisterRequest;
import com.karakoc.scraper.exceptions.general.ForbiddenException;
import com.karakoc.scraper.exceptions.general.UnauthorizatedException;
import com.karakoc.scraper.user.UserDTO;
import com.karakoc.scraper.security.UserPrincipal;
import com.karakoc.scraper.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController

@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;
    private final UserService userService;


    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
         return authService.attemptLogin(request.getEmail(),request.getPassword());
    }

    @PostMapping("/register")
    public UserDTO register(@RequestBody RegisterRequest request) {
       return authService.attemptRegister(request.getEmail(),request.getPassword());
    }
    @GetMapping("/getme")
    public UserDTO getMe(@AuthenticationPrincipal UserPrincipal principal){
        if (principal==null){
            throw new UnauthorizatedException("Unauthorized.");
        }
        return userService.getUserByEmail(principal.getEmail());
    }


}
