package com.inn.restaurant.service;

import com.inn.restaurant.wrapper.UserWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserService {
    ResponseEntity<String> signUp(Map<String,String> requestMap);
    ResponseEntity<String> login(Map<String,String> requestMap);
    ResponseEntity<List<UserWrapper>> getAllUser();
    ResponseEntity<String> updateStatus(Map<String,String> requestMap);
    ResponseEntity<String> updateUser(Map<String, String> requestMap);
    ResponseEntity<String> deleteUser(Integer id);
    ResponseEntity<String> checkToken();
    ResponseEntity<String> changePassword(Map<String, String> requestMap);
}
