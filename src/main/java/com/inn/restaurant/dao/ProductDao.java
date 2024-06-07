package com.inn.restaurant.dao;

import com.inn.restaurant.model.Product;
import com.inn.restaurant.wrapper.ProductWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface ProductDao extends JpaRepository<Product,Integer> {
    List<ProductWrapper> getAllProduct();

    @Modifying
    @Transactional
    Integer updateProductStatus(@Param("status") String status, @Param("id") Integer id);

     List<ProductWrapper> getProductByCategory(@Param("id") Integer id);
    ProductWrapper getProductById(@Param("id") Integer id);
}
