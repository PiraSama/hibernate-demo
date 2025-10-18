package org.example.multilangproduct.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductCategoryTranslationId implements Serializable {

    @Column(name = "ProductCategoryID")
    private Integer productCategoryId; // Khóa ngoại 1

    @Column(name = "LanguageID")
    private String languageId; // Khóa ngoại 2

    public ProductCategoryTranslationId() {}

    public ProductCategoryTranslationId(Integer productCategoryId, String languageId) {
        this.productCategoryId = productCategoryId;
        this.languageId = languageId;
    }

    // --- Getters and Setters ---
    public Integer getProductCategoryId() { return productCategoryId; }
    public void setProductCategoryId(Integer productCategoryId) { this.productCategoryId = productCategoryId; }
    public String getLanguageId() { return languageId; }
    public void setLanguageId(String languageId) { this.languageId = languageId; }

    // --- equals() and hashCode() BẮT BUỘC cho EmbeddedId ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCategoryTranslationId that = (ProductCategoryTranslationId) o;
        return Objects.equals(productCategoryId, that.productCategoryId) && Objects.equals(languageId, that.languageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productCategoryId, languageId);
    }
}