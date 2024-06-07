package com.inn.restaurant.wrapper;

import lombok.Data;


@Data
public class CategoryWrapper {
    Integer id;

    String name;

    public CategoryWrapper() {
    }

    public CategoryWrapper(Integer id, String name) {
        this.id = id;
        this.name = name;
    }


}
