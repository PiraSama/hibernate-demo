package org.example.multilangproduct.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "Language")
public class Language implements Serializable {

    @Id
    @Column(name = "languageId", length = 10)
    private String languageId; // Khóa chính là String

    @Column(name = "languageName", nullable = false, length = 100)
    private String languageName;

    @Column(name = "code", length = 10, unique = true, nullable = false)
    private String code; // Mã ngôn ngữ (ví dụ: "en", "vi")

    @OneToMany(mappedBy = "language")
    private Set<ProductTranslation> productTranslations;

    @OneToMany(mappedBy = "language")
    private Set<ProductCategoryTranslation> categoryTranslations;

    public Language() {}

    // Constructor đầy đủ (dùng cho MainApp)
    public Language(String languageId, String languageName, String code) {
        this.languageId = languageId;
        this.languageName = languageName;
        this.code = code;
    }

    // Constructor đơn giản (Nếu languageId tự tạo, nhưng ta dùng String ID nên cần 3 tham số)
    public Language(String code, String languageName) {
        // Ta vẫn phải set languageId, nên dùng code hoặc một giá trị mặc định.
        this.languageId = code.toUpperCase();
        this.languageName = languageName;
        this.code = code;
    }

    // --- Getters and Setters ---
    public String getLanguageId() { return languageId; }
    public void setLanguageId(String languageId) { this.languageId = languageId; }
    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Set<ProductTranslation> getProductTranslations() { return productTranslations; }
    public void setProductTranslations(Set<ProductTranslation> productTranslations) { this.productTranslations = productTranslations; }
    public Set<ProductCategoryTranslation> getCategoryTranslations() { return categoryTranslations; }
    public void setCategoryTranslations(Set<ProductCategoryTranslation> categoryTranslations) { this.categoryTranslations = categoryTranslations; }
}