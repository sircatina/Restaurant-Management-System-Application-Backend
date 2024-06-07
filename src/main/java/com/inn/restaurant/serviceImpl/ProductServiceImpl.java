package com.inn.restaurant.serviceImpl;

import com.inn.restaurant.JWT.JwtFilter;
import com.inn.restaurant.JWT.JwtUtil;
import com.inn.restaurant.constants.RestaurantConstants;
import com.inn.restaurant.dao.ProductDao;
import com.inn.restaurant.model.Category;
import com.inn.restaurant.model.Product;
import com.inn.restaurant.service.ProductService;
import com.inn.restaurant.utils.RestaurantUtils;
import com.inn.restaurant.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductDao productDAO;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    JwtUtil jwtUtil;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()) {
                if(validateProductMap(requestMap, false)) {
                    productDAO.save(getProductFromMap(requestMap, false));
                    return RestaurantUtils.getResponseEntity("Product Added Successfully!", HttpStatus.OK);
                }
                return RestaurantUtils.getResponseEntity(RestaurantConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            } else
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey("name")) {
            if(requestMap.containsKey("id") && validateId) {
                return true;
            } else if(!validateId) {
                return true;
            }
        }
        return false;
    }
    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));

        Product product = new Product();
        if(isAdd) {
            product.setId(Integer.parseInt(requestMap.get("id")));
        } else {
            product.setStatus("true");
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));
        product.setQuantity(requestMap.get("quantity"));
        return product;
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
        try {
            return new ResponseEntity<>(productDAO.getAllProduct(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()) {
                if(validateProductMap(requestMap, true)) {
                    Optional<Product> optional = productDAO.findById(Integer.parseInt(requestMap.get("id")));
                    if(!optional.isEmpty()) {
                        Product product = getProductFromMap(requestMap, true); // we need to pass it true, so we get the id for this object/product
                        product.setStatus(optional.get().getStatus());
                        productDAO.save(product);
                        return RestaurantUtils.getResponseEntity("Product Updated Successfully!", HttpStatus.OK);
                    } else {
                        return RestaurantUtils.getResponseEntity("Product id doesn't exist.", HttpStatus.OK);
                    }
                } else {
                    return RestaurantUtils.getResponseEntity(RestaurantConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        try {
            if(jwtFilter.isAdmin()){
                Optional optional = productDAO.findById(id);
                if(!optional.isEmpty()) {
                    productDAO.deleteById(id);
                    return RestaurantUtils.getResponseEntity("Product Deleted Successfully!", HttpStatus.OK);
                } else {
                    return RestaurantUtils.getResponseEntity("Product id doesn't exist!", HttpStatus.OK);
                }
            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()) {
                Optional optional = productDAO.findById(Integer.parseInt(requestMap.get("id")));
                if(!optional.isEmpty()) {
                    productDAO.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    return RestaurantUtils.getResponseEntity("Status Updated Successfully!", HttpStatus.OK);
                } else {
                    return RestaurantUtils.getResponseEntity("Product id doesn't exist!", HttpStatus.OK);
                }
            } else {
                return RestaurantUtils.getResponseEntity(RestaurantConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WRONG, HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        try {
            return new ResponseEntity<>(productDAO.getProductByCategory(id), HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProductWrapper> getById(Integer id) {
        try {
            return new ResponseEntity<>(productDAO.getProductById(id), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
