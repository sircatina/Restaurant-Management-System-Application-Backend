package com.inn.restaurant.model;

import jdk.jfr.Name;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

@NamedQuery(name = "Category.getAllCategory", query = "SELECT c from Category c where c.id in (SELECT p.category from Product p where p.status='true')")
@NamedQuery(name = "Category.getCategoryById", query = "SELECT new com.inn.restaurant.wrapper.CategoryWrapper(c.id, c.name) FROM Category c where c.id=:id")

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "category")
public class Category implements Serializable {
    private static final long serialVersionUID=1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;
    @Column(name="name")
    private String name;


}
