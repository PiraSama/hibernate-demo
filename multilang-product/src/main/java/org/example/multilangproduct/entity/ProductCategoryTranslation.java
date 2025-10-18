package org.example.multilangproduct.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ProductCategoryTranslation")
public class ProductCategoryTranslation implements Serializable {

    @EmbeddedId
    private ProductCategoryTranslationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productCategoryId")
    @JoinColumn(name = "ProductCategoryID", insertable = false, updatable = false)
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("languageId")
    @JoinColumn(name = "LanguageID", insertable = false, updatable = false)
    private Language language;

    @Column(name = "CategoryName", nullable = false, length = 100)
    private String categoryName;

    public ProductCategoryTranslation() {}

    // Constructor đã được sửa đổi (Dùng language.getLanguageId() thay vì code)
    public ProductCategoryTranslation(ProductCategory category, Language language, String categoryName) {
        this.id = new ProductCategoryTranslationId(category.getProductCategoryId(), language.getLanguageId());
        this.category = category;
        this.language = language;
        this.categoryName = categoryName;
    }

    // --- Getters and Setters ---
    public ProductCategoryTranslationId getId() { return id; }
    public void setId(ProductCategoryTranslationId id) { this.id = id; }
    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}