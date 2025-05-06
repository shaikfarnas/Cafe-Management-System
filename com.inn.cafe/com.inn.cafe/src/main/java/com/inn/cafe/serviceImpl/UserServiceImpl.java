package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUsersDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtils;
import com.inn.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Slf4j //for login purpose
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;

    @Autowired
     AuthenticationManager authenticationManager;
    @Autowired
     CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside signup {}", requestMap);
        try {
            // Validate the signup data
            if (validateSignUpMap(requestMap)) {
                // Check if the user already exists by email
                User user = userDao.findByEmailId(requestMap.get("email"));

                if (Objects.isNull(user)) {
                    // Save new user to the database
                    userDao.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity("Successfully registered.", HttpStatus.OK);
                } else {
                    // If the user already exists, return a bad request response
                    return CafeUtils.getResponseEntity("Email already exists.", HttpStatus.BAD_REQUEST);
                }
            } else {
                // If validation fails, return invalid data response
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            // Log the exception for debugging
            log.error("Error occurred during signup", ex);
        }
        // Return internal server error if an exception occurs
        return CafeUtils.getResponseEntity(CafeUtils.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        if (requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email") && requestMap.containsKey("password")) {

            return true;
        }
        return false;
    }
    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(passwordEncoder.encode(requestMap.get("password")));
        user.setStatus("false");
        user.setRole("user");
        return user;
    }
    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login with requestMap: {}", requestMap);
        try {
            String email = requestMap.get("email");
            String password = requestMap.get("password");

            if (email == null || password == null) {
                log.error("Email or password missing");
                return new ResponseEntity<>(
                        "{\"message\":\"Email and password are required.\"}",
                        HttpStatus.BAD_REQUEST
                );
            }
            log.info("Attempting authentication for email: {}", email);
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            if (auth.isAuthenticated()) {
                log.info("Authentication successful for email: {}", email);
                var userDetails = customerUsersDetailsService.getUserDetail();
                log.info("User details retrieved: {}", userDetails);
                if (userDetails != null && "true".equalsIgnoreCase(userDetails.getStatus())) {
                    log.info("User is active, generating token.");
                    String token = jwtUtil.generateToken(userDetails.getEmail(), userDetails.getRole());
                    log.info("Token generated successfully for email: {}", email);
                    return new ResponseEntity<>(
                            "{\"token\":\"" + token + "\"}",
                            HttpStatus.OK
                    );
                } else {
                    System.out.println("User is not approved, returning approval message.");
                    log.warn("User is not approved, returning approval message.");
                    return new ResponseEntity<>(
                            "{\"message\":\"Wait for admin approval.\"}",
                            HttpStatus.BAD_REQUEST
                    );
                }}} catch (AuthenticationException ex) {
            log.error("Authentication failed for email: {} - {}", requestMap.get("email"), ex.getMessage());
            return new ResponseEntity<>(
                    "{\"message\":\"Invalid credentials.\"}",
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Exception ex) {
            log.error("Unexpected error during login", ex);
            return new ResponseEntity<>(
                    "{\"message\":\"An error occurred. Please try again later.\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return new ResponseEntity<>(
                "{\"message\":\"Bad Credentials.\"}",
                HttpStatus.BAD_REQUEST);}



    @Override
    public ResponseEntity<List<UserWrapper>> getAllUserlogout() {
       try
        {
            if(jwtFilter.isAdmin())
            {
                  return new ResponseEntity<>(userDao.getAllUser(),HttpStatus.OK);

            }
            else
            {
                return new ResponseEntity<>(new ArrayList<>(),HttpStatus.UNAUTHORIZED);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try{
            if(jwtFilter.isAdmin())
            {
                return new ResponseEntity<>(userDao.getAllUser(),HttpStatus.OK);

            }
            else
            {
                return new ResponseEntity<>(new ArrayList<>(),HttpStatus.UNAUTHORIZED);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return  new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            log.info("Update request received: {}", requestMap);

            if (jwtFilter.isAdmin()) {
                log.info("Admin access granted.");
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if (optional.isPresent()) {
                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmin(requestMap.get("status"),optional.get().getEmail(),userDao.getAllAdmin());
                    return CafeUtils.getResponseEntity("User Status Updated Successfully", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("User id doesn't exist", HttpStatus.BAD_REQUEST);
                }
            } else {
                log.warn("Unauthorized access attempt.");
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            log.error("Error updating user status: {}", ex.getMessage(), ex);
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUser());
        if(status!=null &&status.equalsIgnoreCase("true")){
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),"Account Approved","USER:- "+user+" \n is approved by \nADMIN:-"+jwtFilter.getCurrentUser(),allAdmin);

        }
        else {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),"Account Disabled","USER:- "+user+" \n is disabled by \nADMIN:-"+jwtFilter.getCurrentUser(),allAdmin);

        }


    }
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/checkToken")
    @Override
    public ResponseEntity<String> checkToken() {
        return CafeUtils.getResponseEntity("true",HttpStatus.OK);

    }
    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            // Fetch the user from the database using email from the JWT token
            String email = jwtFilter.getCurrentUser();
            User userObj = userDao.findByEmail(email);

            if (userObj != null) {
                // Verify the old password matches the stored hashed password
                String oldPassword = requestMap.get("oldPassword");
                if (passwordEncoder.matches(oldPassword, userObj.getPassword())) {
                    // Hash and set the new password
                    String newPassword = passwordEncoder.encode(requestMap.get("newPassword"));
                    userObj.setPassword(newPassword);
                    userDao.save(userObj);

                    // Return a String message
                    return new ResponseEntity<>("Password Updated Successfully", HttpStatus.OK);
                } else {
                    // Return an error message for incorrect old password
                    return new ResponseEntity<>("Incorrect Old Password", HttpStatus.BAD_REQUEST);
                }
            } else {
                // Return an error message if user is not found
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // Return a generic error message in case of internal error
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*@Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmail(requestMap.get("email"));
            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                emailUtils.forgotMail(user.getEmail(), "Credentials by Cafe Management System", user.getPassword());
                return CafeUtils.getResponseEntity("Check your mail for credentials.", HttpStatus.OK);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
*/
    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmail(requestMap.get("email"));
            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                emailUtils.forgotMail(user.getEmail(), "Credentials by Cafe Management System", user.getPassword());
                return CafeUtils.getResponseEntity("Check your mail for credentials.", HttpStatus.OK);
            } else {
                log.warn("No user found for email: {}", requestMap.get("email"));
                return CafeUtils.getResponseEntity("Invalid email address.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error in forgotPassword: {}", ex.getMessage(), ex);
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
