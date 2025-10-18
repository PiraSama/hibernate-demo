package org.example.multilangproduct.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "Product")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Sửa lỗi DDL: Auto-increment
    @Column(name = "ProductID")
    private Integer productId;

    @Column(name = "Price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "Weight", precision = 4, scale = 2)
    private BigDecimal weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductCategoryID", nullable = false)
    private ProductCategory category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductTranslation> translations;

    public Product() {}

    public Product(BigDecimal price, BigDecimal weight, ProductCategory category) {
        this.price = price;
        this.weight = weight;
        this.category = category;
    }

    // --- Getters and Setters ---
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }
    public Set<ProductTranslation> getTranslations() { return translations; }
    public void setTranslations(Set<ProductTranslation> translations) { this.translations = translations; }
}