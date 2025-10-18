package org.example.multilangproduct.dto;

import java.math.BigDecimal;

public class ProductDisplayDTO {
    private Integer productId;
    private String productName;
    private BigDecimal price;
    private String categoryName;
    private String productDescription; // Chỉ dùng cho chi tiết sản phẩm (getters/setters riêng)

    // Constructor đã được đồng bộ hóa với Service
    public ProductDisplayDTO(Integer productId, String productName, BigDecimal price, String categoryName) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.categoryName = categoryName;
    }

    // --- Getters và Setters ---
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    // Getter/Setter cho Mô tả (dùng cho Detail)
    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
}