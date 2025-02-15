package com.inn.restaurant.wrapper;

import lombok.Data;

@Data
public class  ProductWrapper {
    Integer id;

    String name;

    String description;

    Integer price;

    String status;
    String quantity;

    Integer categoryId;

    String categoryName;

    public ProductWrapper(){}

    public ProductWrapper(Integer id, String name, String description,
                          Integer price, String status,String quantity, Integer categoryId, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public ProductWrapper(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProductWrapper(Integer id, String name, String description, Integer price,String quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity=quantity;
    }
}
