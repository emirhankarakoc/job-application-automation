package com.karakoc.scraper.account;

import com.karakoc.scraper.account.requests.LoginResponse;
import com.karakoc.scraper.user.UserDTO;

public interface AuthService {
    LoginResponse attemptLogin(String email, String password);
    UserDTO attemptRegister(String email, String password);
}
