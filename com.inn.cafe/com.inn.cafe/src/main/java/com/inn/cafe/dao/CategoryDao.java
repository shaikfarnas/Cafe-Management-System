package com.inn.cafe.dao;

import com.inn.cafe.POJO.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryDao extends JpaRepository <Category, Integer>{
    @Query("SELECT c FROM Category c")
    List<Category> getAllCategories();

    List<Category> getAllCategory();
}
