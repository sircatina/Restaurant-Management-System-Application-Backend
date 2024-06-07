package com.inn.restaurant.serviceImpl;

import com.inn.restaurant.JWT.CustomerUserDetailsService;
import com.inn.restaurant.JWT.JwtFilter;
import com.inn.restaurant.JWT.JwtUtil;
import com.inn.restaurant.constants.RestaurantConstants;
import com.inn.restaurant.dao.UserDao;
import com.inn.restaurant.model.User;
import com.inn.restaurant.service.UserService;
import com.inn.restaurant.utils.EmailUtils;
import com.inn.restaurant.utils.RestaurantUtils;
import com.inn.restaurant.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    CustomerUserDetailsService customerUserDetailsService;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside signup {}", requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userDao.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    // Hash the password before saving
                    String hashedPassword = passwordEncoder.encode(requestMap.get("password"));
                    requestMap.put("password", hashedPassword);

                    userDao.save(getUserFromMap(requestMap, false));
                    return RestaurantUtils.getResponseEntity("Successfully Registered.", HttpStatus.OK);
                } else {
                    return RestaurantUtils.getResponseEntity("Email already exists.", HttpStatus.BAD_REQUEST);
                }
            } else {
                return RestaurantUtils.getResponseEntity((RestaurantConstants.INVALID_DATA), HttpStatus.BAD_REQUEST);
            }
        } catch (IllegalArgumentException ex) {
            // Handle validation errors
            return RestaurantUtils.getResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            // Handle other exceptions
            log.error("An error occurred during sign up.", ex);
            return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateSignUpMap( Map<String,String> requestMap){
        return requestMap.containsKey("name")
                && requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email")
                && requestMap.containsKey("password");
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-Z]+");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.endsWith("@gmail.com");
    }

    private boolean isValidContactNumber(String contactNumber) {
        return contactNumber != null && contactNumber.matches("^07\\d{8}$");
    }

    private User getUserFromMap(Map<String,String> requestMap, boolean isAdd){
        User user = new User();
        if(isAdd) {
            user.setId(Integer.parseInt(requestMap.get("id")));
        }
        String name = requestMap.get("name");
        if (isValidName(name)) {
            user.setName(name);
        } else {
            throw new IllegalArgumentException("Name should contain only characters.");
        }
        String contactNumber = requestMap.get("contactNumber");
        if (isValidContactNumber(contactNumber)) {
            user.setContactNumber(contactNumber);
        } else {
            throw new IllegalArgumentException("Contact number should have 10 digits and start with '07'.(e codat in retea de Romania)");
        }
        String email = requestMap.get("email");
        if (isValidEmail(email)) {
            user.setEmail(email);
        } else {
            throw new IllegalArgumentException("Email should end with @gmail.com.");
        }
        user.setPassword(requestMap.get("password"));
        user.setRole("user");
        user.setStatus("true");
        return user;
    }


    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmailId(requestMap.get("email"));
            if (user != null && passwordEncoder.matches(requestMap.get("password"), user.getPassword())) {
                // Generate JWT token for the authenticated user
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                return ResponseEntity.ok("{\"token\": \"" + token + "\"}");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
            }
        } catch(Exception ex){
            log.error("Login failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestaurantConstants.SOMETHING_WRONG);
        }
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            if(jwtFilter.isAdmin()) {
                return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);

            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()) {
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if(!optional.isEmpty()) {
                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
                    return RestaurantUtils.getResponseEntity("User status updated successfully", HttpStatus.OK);

                } else {
                    RestaurantUtils.getResponseEntity("User id does not exist", HttpStatus.OK);
                }

            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUser());
        if(status!=null && status.equalsIgnoreCase("true")){
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Approved", "USER:- "+user+"\n is approved by \nADMIN:-"+jwtFilter.getCurrentUser(),allAdmin);
        }
        else {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled", "USER:- "+user+"\n is disabled by \nADMIN:-"+jwtFilter.getCurrentUser(),allAdmin);
        }
    }

    @Override
    public ResponseEntity<String> deleteUser(Integer id) {
        try {
            if(jwtFilter.isAdmin()) {
                Optional optional = userDao.findById(id);
                if (!optional.isEmpty()) {
                    userDao.deleteById(id);
                    return RestaurantUtils.getResponseEntity("User Deleted Successfully!", HttpStatus.OK);
                } else {
                    return RestaurantUtils.getResponseEntity("User id doesn't exist!", HttpStatus.OK);

                }
            }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        private boolean validateUserMap(Map<String, String> requestMap, boolean validateId) {
            if(requestMap.containsKey("name")) {
                if(requestMap.containsKey("id") && validateId) {
                    return true;
                } else if(!validateId) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public ResponseEntity<String> updateUser(Map<String, String> requestMap) {
            try {
                if (jwtFilter.isAdmin()) {
                    if (validateUserMap(requestMap, true)) {
                        Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
                        if (optional.isPresent()) {
                            userDao.save(getUserFromMap(requestMap, true));
                            return RestaurantUtils.getResponseEntity("User Updated Successfully!", HttpStatus.OK);
                        } else {
                            return RestaurantUtils.getResponseEntity("User id doesn't exist!", HttpStatus.OK);
                        }
                    }
                    return RestaurantUtils.getResponseEntity(RestaurantConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                } else {
                    return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
                }
            } catch (IllegalArgumentException ex) {
                // Handle validation errors
                return RestaurantUtils.getResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (Exception ex) {
                // Handle other exceptions
                log.error("An error occurred during user update.", ex);
                return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        @Override
        public ResponseEntity<String> checkToken() {
            return RestaurantUtils.getResponseEntity("true", HttpStatus.OK);
        }

        @Override
        public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
            try {
                User userObj = userDao.findByEmailId(jwtFilter.getCurrentUser());
                if (userObj != null) { // Check if userObj is not null before accessing it
                    if (passwordEncoder.matches(requestMap.get("oldPassword"), userObj.getPassword())) {
                        // Hash the new password before updating
                        String newPassword = passwordEncoder.encode(requestMap.get("newPassword"));
                        userObj.setPassword(newPassword);
                        userDao.save(userObj);
                        return RestaurantUtils.getResponseEntity("Password Updated Successfully!", HttpStatus.OK);
                    }
                    return RestaurantUtils.getResponseEntity("Incorrect Old Password.", HttpStatus.BAD_REQUEST);
                }
                return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
