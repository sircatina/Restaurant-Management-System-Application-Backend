package com.inn.restaurant.model;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

@NamedQuery(name = "Product.getAllProduct", query = "SELECT new com.inn.restaurant.wrapper.ProductWrapper(p.id, p.name, p.description, p.price, p.status,p.quantity, p.category.id, p.category.name) from Product p")

@NamedQuery(name = "Product.updateProductStatus", query = "UPDATE Product p set p.status=:status where p.id=:id")

@NamedQuery(name = "Product.getProductByCategory", query = "SELECT new com.inn.restaurant.wrapper.ProductWrapper(p.id, p.name) from Product p where p.category.id=:id and p.status='true'")

@NamedQuery(name = "Product.getProductById", query = "SELECT new com.inn.restaurant.wrapper.ProductWrapper(p.id, p.name, p.description, p.price, p.quantity) FROM Product p where p.id=:id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="product")
public class Product implements Serializable{
        private static final long serialVersionUID=123456L;
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name="id")
        private Integer id;
        @Column(name="name")
        private String name;

        @Column(name="description")
        private String description;
        @Column(name = "price")
        private Integer price;
        @Column(name = "quantity")
        private String quantity;

        @Column(name = "status")
        private String status;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "category_fk", nullable = false)
        private Category category;
}
