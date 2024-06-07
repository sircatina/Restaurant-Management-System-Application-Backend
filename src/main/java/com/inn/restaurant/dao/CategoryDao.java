package com.inn.restaurant.dao;

import com.inn.restaurant.model.Category;
import com.inn.restaurant.wrapper.CategoryWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryDao extends JpaRepository<Category,Integer> {

    List<Category> getAllCategory();
    CategoryWrapper getCategoryById(@Param("id") Integer id);
}
