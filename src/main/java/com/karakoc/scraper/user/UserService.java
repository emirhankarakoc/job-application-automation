package com.karakoc.scraper.user;


import com.karakoc.scraper.orderrequests.OrderRequest;
import com.karakoc.scraper.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserDTO createUser(String email, String password);
    UserDTO getUserByEmail(String email);
    UserDTO postAdditionalDetails(String userId, UserController.SendAdditionalDetailsRequest r);

        UserDTO getUserById(String id);
    String deleteUser(String email);
    List<UserDTO> getAllUsers();
    UserDTO setUserCvSummary(String userId,String cvSummary);
    ResponseEntity<UserManager.GetUserCvSummary> getUserCvSummary(String userId);

    ResponseEntity<UserManager.SetUserCvFileResponse> setUserCvFile(String userId, MultipartFile file);
    ResponseEntity<UserManager.GetUserCvFileResponse> getUserCvFile(String userId);
    List<OrderRequest> getAllOrdersByUserId(String userId);

    }
