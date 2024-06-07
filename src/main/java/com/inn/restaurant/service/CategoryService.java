package com.inn.restaurant.service;

import com.inn.restaurant.model.Category;
import com.inn.restaurant.wrapper.CategoryWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    ResponseEntity<String> addNewCategory(Map<String, String> requestMap);
    ResponseEntity<List<Category>> getAllCategory(String filterValue);
    ResponseEntity<String> updateCategory(Map<String, String> requestMap);
    ResponseEntity<CategoryWrapper> getById(Integer id);
    ResponseEntity<String> deleteCategory(Integer id);
}
