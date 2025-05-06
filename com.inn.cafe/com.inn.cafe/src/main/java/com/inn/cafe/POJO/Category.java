package com.inn.cafe.POJO;
import jakarta.persistence.*;
//import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
//where c.id in (select p.category from Product p where p.status='true')
@NamedQuery(name="Category.getAllCategory",query="select c from Category c ")

@Data//going to control all the below code
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="name")
    private String name;




}
