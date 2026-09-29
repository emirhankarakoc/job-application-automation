package com.karakoc.scraper.user;

import com.karakoc.scraper.cloudflare.R2Service;
import com.karakoc.scraper.exceptions.general.BadRequestException;
import com.karakoc.scraper.exceptions.general.NotfoundException;
import com.karakoc.scraper.exceptions.strings.ExceptionMessages;
import com.karakoc.scraper.orderrequests.OrderRequest;
import com.karakoc.scraper.orderrequests.OrderRequestsRepository;
import com.karakoc.scraper.security.WebSecurityConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.karakoc.scraper.user.User.userToDTO;
import static com.karakoc.scraper.user.User.usersToDTOS;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserManager implements UserService{

    private final UserRepository repository;
    private final ExceptionMessages messages;
    private final R2Service r2Service;
    private final WebSecurityConfig webSecurityConfig;
    private final OrderRequestsRepository orderRequestsRepository;

    @Override
    public UserDTO createUser(String email, String password) {

        if (repository.findUserByEmail(email).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setPassword(webSecurityConfig.passwordEncoder().encode(password));
        user.setRole(Roles.ROLE_USER.toString());
        return User.userToDTO(repository.save(user));
    }
    @Override
    public UserDTO getUserByEmail(String email){
        User user = repository.findUserByEmail(email).orElseThrow(()-> new NotfoundException(messages.getUSER_NOT_FOUND_404()));
        var dto = User.userToDTO(user);
        return dto;
    }

    @Override
    public UserDTO getUserById(String id) {
        User user = repository.findUserByEmail(id).orElseThrow(()-> new NotfoundException(messages.getUSER_NOT_FOUND_404()));
        var dto = User.userToDTO(user);
        return dto;
    }

    @Override
    public String deleteUser(String email){
        User user = repository.findUserByEmail(email).orElseThrow(()-> new NotfoundException(messages.getUSER_NOT_FOUND_404()));
        repository.delete(user);
        return "An user deleted with given email adress:" + user.getEmail();
    }


    public List<UserDTO> getAllUsers(){
        var allusers = repository.findAll();
        return usersToDTOS(allusers);
    }

    public UserDTO setUserCvSummary(String userId,String cvSummary){
        User user = repository.findById(userId).orElseThrow(()-> new NotfoundException(messages.getUSER_NOT_FOUND_404()));
        user.setCvSummary(cvSummary);
        user.setCvSummaryUpdatedAt(LocalDateTime.now());
        repository.save(user);
        return User.userToDTO(user);
    }


    public record GetUserCvSummary(String cvSummary, String cvSummaryUpdatedAt){}
    @Override
    public ResponseEntity<GetUserCvSummary> getUserCvSummary(String userId) {
        User user = repository.findById(userId).orElseThrow(()-> new NotfoundException(messages.getUSER_NOT_FOUND_404()));
        if (user.getCvSummary() == null) {
            user.setCvSummary("Nothing to show");
            user.setCvSummaryUpdatedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok(new GetUserCvSummary(user.getCvSummary(),user.getCvSummaryUpdatedAt().toString()));
    }


    public record SetUserCvFileResponse(String publicUrl, String fileName,String cvFileUploadedAt){}
    @Override
    public ResponseEntity<SetUserCvFileResponse> setUserCvFile(String userId, MultipartFile file) {
        User user = repository.findById(userId).orElseThrow(()-> new NotfoundException(messages.getUSER_NOT_FOUND_404()));
        if (user.getCvFileName()!=null) {
            //if there is any file , delete previous one. so we don;t have to store communities files.
            r2Service.destroy(user.getCvUrlKey());
        }

        String cloudflareKey = r2Service.uploadFile(file);
        user.setCvUrlKey(cloudflareKey);
        user.setCvFileName(file.getOriginalFilename());
        user.setCvFileUpdatedAt(LocalDateTime.now());
        repository.save(user);
        return ResponseEntity.ok(new SetUserCvFileResponse(r2Service.getPublicUrl(cloudflareKey),file.getOriginalFilename(),LocalDateTime.now().toString() ));
    }

    public record GetUserCvFileResponse(String publicUrl,String fileName, LocalDateTime cvFileUpdatedAt){}

    /**
     * @param userId
     * @return
     */
    @Override
    public ResponseEntity<GetUserCvFileResponse> getUserCvFile(String userId) {
        User user = repository.findById(userId).orElseThrow(()-> new NotfoundException(messages.getUSER_NOT_FOUND_404()));
        String publicUrl = r2Service.getPublicUrl(user.getCvUrlKey());

        return ResponseEntity.ok(new GetUserCvFileResponse(publicUrl,user.getCvFileName(),user.getCvFileUpdatedAt()));
        }

    @Override
    public List<OrderRequest> getAllOrdersByUserId(String userId) {
       return  orderRequestsRepository.findAllByUserId(userId);
    }



    @Override
    public UserDTO postAdditionalDetails(String userId,UserController.SendAdditionalDetailsRequest r) {
        User user = repository.findById(userId).orElseThrow(()->new NotfoundException("User not found."));
        user.setFullname(r.fullname());
        user.setPhoneNumber(r.phoneNumber());
        user.setEmailForMailSending(r.emailForMailSending());
        user.setAddress(r.address());
        user.setGithubUrl(r.githubUrl());
        user.setLinkedinUrl(r.linkedinUrl());
        user.setWebsite(r.website());

        repository.save(user);
        return userToDTO(user);
    }
}
