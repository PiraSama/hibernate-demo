package org.example.multilangproduct.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ProductTranslation")
public class ProductTranslation implements Serializable {

    @EmbeddedId
    private ProductTranslationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "ProductID", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("languageId")
    @JoinColumn(name = "LanguageID", insertable = false, updatable = false)
    private Language language;

    @Column(name = "ProductName", nullable = false, length = 200)
    private String productName;

    @Column(name = "ProductDescription", length = 255)
    private String productDescription;

    public ProductTranslation() {}

    public ProductTranslation(Product product, Language language, String productName, String productDescription) {
        this.id = new ProductTranslationId(product.getProductId(), language.getLanguageId());
        this.product = product;
        this.language = language;
        this.productName = productName;
        this.productDescription = productDescription;
    }

    // --- Getters and Setters ---
    public ProductTranslationId getId() { return id; }
    public void setId(ProductTranslationId id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
}