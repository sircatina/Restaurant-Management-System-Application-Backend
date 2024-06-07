package com.inn.restaurant.serviceImpl;
import com.inn.restaurant.JWT.CustomerUserDetailsService;
import com.inn.restaurant.JWT.JwtFilter;
import com.inn.restaurant.JWT.JwtUtil;
import com.inn.restaurant.constants.RestaurantConstants;
import com.inn.restaurant.dao.UserDao;
import com.inn.restaurant.model.User;
import com.inn.restaurant.serviceImpl.UserServiceImpl;
import com.inn.restaurant.utils.EmailUtils;
import com.inn.restaurant.utils.RestaurantUtils;
import com.inn.restaurant.wrapper.UserWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;



import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserDao userDao;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomerUserDetailsService customerUserDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtFilter jwtFilter;

    @Mock
    private EmailUtils emailUtils;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignUpSuccess() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("name", "Ioana");
        requestMap.put("contactNumber", "0734567033");
        requestMap.put("email", "gol@gmail.com");
        requestMap.put("password", "123456");

        // Mock UserDao to return null for findByEmailId
        when(userDao.findByEmailId(anyString())).thenReturn(null);
        // Mock UserDao to return null when save is called
        when(userDao.save(any(User.class))).thenReturn(null);

        ResponseEntity<String> response = userService.signUp(requestMap);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"message\":\"Successfully Registered.\"}", response.getBody());
    }





    @Test
    void testSignUpEmailExists() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("name", "John");
        requestMap.put("contactNumber", "0712345678");
        requestMap.put("email", "john@gmail.com");
        requestMap.put("password", "password");

        when(userDao.findByEmailId(anyString())).thenReturn(new User());

        ResponseEntity<String> response = userService.signUp(requestMap);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("{\"message\":\"Email already exists.\"}", response.getBody());
    }

    @Test
    void testSignUpInvalidData() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("name", "John");

        ResponseEntity<String> response = userService.signUp(requestMap);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("{\"message\":\"Invalid Data.\"}", response.getBody());
    }

    @Test
    void testGetAllUser() {
        List<UserWrapper> userList = new ArrayList<>();
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(userDao.getAllUser()).thenReturn(userList);

        ResponseEntity<List<UserWrapper>> response = userService.getAllUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userList, response.getBody());
    }

    @Test
    void testGetAllUserUnauthorized() {
        when(jwtFilter.isAdmin()).thenReturn(false);

        ResponseEntity<List<UserWrapper>> response = userService.getAllUser();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    @Test
    void testUpdateStatusSuccess() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("id", "1");
        requestMap.put("status", "true");

        User user = new User();
        user.setEmail("john@gmail.com");

        when(jwtFilter.isAdmin()).thenReturn(true);
        when(userDao.findById(anyInt())).thenReturn(Optional.of(user));
        when(userDao.getAllAdmin()).thenReturn(Collections.singletonList("admin@gmail.com"));

        ResponseEntity<String> response = userService.updateStatus(requestMap);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"message\":\"User status updated successfully\"}", response.getBody());
    }

    @Test
    void testUpdateStatusUnauthorized() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("id", "1");
        requestMap.put("status", "true");

        when(jwtFilter.isAdmin()).thenReturn(false);

        ResponseEntity<String> response = userService.updateStatus(requestMap);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("{\"message\":\"Unauthorized access.\"}", response.getBody());
    }

    @Test
    void testDeleteUserSuccess() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(userDao.findById(anyInt())).thenReturn(Optional.of(new User()));

        ResponseEntity<String> response = userService.deleteUser(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"message\":\"User Deleted Successfully!\"}", response.getBody());
    }

    @Test
    void testDeleteUserNotFound() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(userDao.findById(anyInt())).thenReturn(Optional.empty());

        ResponseEntity<String> response = userService.deleteUser(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"message\":\"User id doesn't exist!\"}", response.getBody());
    }

    @Test
    void testCheckToken() {
        ResponseEntity<String> response = userService.checkToken();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"message\":\"true\"}", response.getBody());
    }

}