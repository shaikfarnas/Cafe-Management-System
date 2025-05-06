package com.inn.cafe.wrapper;

import lombok.Data;

@Data
public class ProductWrapper {
    Integer id;

    String name;

    String description;

    Integer price;

    String status;

    Integer categoryId;

    String categoryName;

    public ProductWrapper()
    {

    }
    public ProductWrapper(Integer id, String name, String description, Integer price, String status, Integer categoryid, String categoryName)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.categoryId = categoryid;
        this.categoryName = categoryName;
    }

    public  ProductWrapper(Integer id, String name){
        this.id = id;
        this.name = name;
    }
    public ProductWrapper(Integer id, String name, String description, Integer price){
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
