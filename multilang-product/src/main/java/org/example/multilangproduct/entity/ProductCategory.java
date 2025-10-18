package org.example.multilangproduct.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "ProductCategory")
public class ProductCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Sửa lỗi DDL: Auto-increment
    @Column(name = "ProductCategoryID")
    private Integer productCategoryId;

    @Column(name = "CanBeShipped", nullable = false)
    private boolean canBeShipped;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductCategoryTranslation> translations;

    @OneToMany(mappedBy = "category")
    private Set<Product> products;

    public ProductCategory() {}

    public ProductCategory(boolean canBeShipped) {
        this.canBeShipped = canBeShipped;
    }

    // --- Getters and Setters ---
    public Integer getProductCategoryId() { return productCategoryId; }
    public void setProductCategoryId(Integer productCategoryId) { this.productCategoryId = productCategoryId; }
    public boolean isCanBeShipped() { return canBeShipped; }
    public void setCanBeShipped(boolean canBeShipped) { this.canBeShipped = canBeShipped; }
    public Set<ProductCategoryTranslation> getTranslations() { return translations; }
    public void setTranslations(Set<ProductCategoryTranslation> translations) { this.translations = translations; }
    public Set<Product> getProducts() { return products; }
    public void setProducts(Set<Product> products) { this.products = products; }

    @Override
    public String toString() {
        return "ProductCategory{" +
                "id=" + productCategoryId +
                ", canBeShipped=" + canBeShipped +
                '}';
    }
}