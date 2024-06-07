package com.inn.restaurant;

import com.inn.restaurant.JWT.JwtUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLOutput;

@SpringBootApplication
public class RestaurantManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestaurantManagementSystemApplication.class, args);

//		JwtUtil jwtUtil=new JwtUtil();
//		String token= jwtUtil.generateToken("tina@gmail.com","admin");
//		System.out.println(token);
	}

}
