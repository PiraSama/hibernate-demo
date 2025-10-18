package org.example.multilangproduct.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductTranslationId implements Serializable {

    @Column(name = "ProductID")
    private Integer productId;

    @Column(name = "LanguageID")
    private String languageId;

    public ProductTranslationId() {}

    public ProductTranslationId(Integer productId, String languageId) {
        this.productId = productId;
        this.languageId = languageId;
    }

    // --- equals() and hashCode() BẮT BUỘC cho EmbeddedId ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductTranslationId that = (ProductTranslationId) o;
        return Objects.equals(productId, that.productId) && Objects.equals(languageId, that.languageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, languageId);
    }

    // --- Getters and Setters ---
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getLanguageId() { return languageId; }
    public void setLanguageId(String languageId) { this.languageId = languageId; }
}