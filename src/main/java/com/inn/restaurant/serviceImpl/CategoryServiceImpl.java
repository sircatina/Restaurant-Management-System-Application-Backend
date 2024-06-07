package com.inn.restaurant.serviceImpl;

import com.inn.restaurant.JWT.JwtFilter;
import com.inn.restaurant.constants.RestaurantConstants;
import com.inn.restaurant.dao.CategoryDao;
import com.inn.restaurant.dao.ProductDao;
import com.inn.restaurant.model.Product;
import com.inn.restaurant.service.CategoryService;
import com.inn.restaurant.model.Category;
import com.inn.restaurant.utils.RestaurantUtils;
import com.google.common.base.Strings;
import com.inn.restaurant.wrapper.CategoryWrapper;
import com.inn.restaurant.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Slf4j
@Service


public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    ProductDao productDao;

    @Autowired
    JwtFilter jwtFilter;

    public CategoryServiceImpl(CategoryDao categoryDao, ProductDao productDao, JwtFilter jwtFilter) {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
        this.jwtFilter= jwtFilter;
    }

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()) {
                if(validateCategoryMap(requestMap, false)) {
                    categoryDao.save(getCategoryFromMap(requestMap, false));
                    return RestaurantUtils.getResponseEntity("Category Added Successfully!", HttpStatus.OK);
                }
            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey("name")) {
            if(requestMap.containsKey("id") && validateId) {
                return true;
            } else if(!validateId) {
                return true;
            }
        }
        return false;
    }

    private Category getCategoryFromMap(Map<String, String> requestMap, Boolean isAdd) {
        Category category = new Category();
        if(isAdd) {
            category.setId(Integer.parseInt(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try {
            if(!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")) {
                log.info("Inside if");
                return new ResponseEntity<List<Category>>(categoryDao.getAllCategory(), HttpStatus.OK);
            }
            return new ResponseEntity<>(categoryDao.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<List<Category>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()) {
                if(validateCategoryMap(requestMap, true)){
                    Optional optional = categoryDao.findById(Integer.parseInt(requestMap.get("id")));
                    if(!optional.isEmpty()) {
                        categoryDao.save(getCategoryFromMap(requestMap, true));
                        return RestaurantUtils.getResponseEntity("Category Updated Successfully!", HttpStatus.OK);
                    } else {
                        return RestaurantUtils.getResponseEntity("Category id doesn't exist!", HttpStatus.OK);
                    }
                }
                return RestaurantUtils.getResponseEntity(RestaurantConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<CategoryWrapper> getById(Integer id) {
        try {
            return new ResponseEntity<>(categoryDao.getCategoryById(id), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new CategoryWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<String> deleteCategory(Integer id) {
        try {
            if(jwtFilter.isAdmin()){
                Optional optional = categoryDao.findById(id);
                if(!optional.isEmpty()) {
                    List<ProductWrapper> products = productDao.getProductByCategory(id);

                    // Delete each product associated with the category
                    for (ProductWrapper product : products) {
                        productDao.deleteById(product.getId());
                    }
                    categoryDao.deleteById(id);
                    return RestaurantUtils.getResponseEntity("Category Deleted Successfully!", HttpStatus.OK);
                } else {
                    return RestaurantUtils.getResponseEntity("Category id doesn't exist!", HttpStatus.OK);
                }
            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
