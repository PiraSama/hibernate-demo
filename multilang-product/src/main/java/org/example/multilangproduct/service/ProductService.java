package org.example.multilangproduct.service;

import org.example.multilangproduct.dao.LanguageDAO;
import org.example.multilangproduct.dao.ProductDAO;
import org.example.multilangproduct.dao.ProductCategoryDAO;
import org.example.multilangproduct.entity.*;
import org.example.multilangproduct.dto.ProductDisplayDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lớp Service chứa logic nghiệp vụ cho Product và Translation.
 * Lớp này sử dụng các DAO TỰ MỞ/ĐÓNG SESSION cho mỗi thao tác.
 */
public class ProductService {

    private final ProductDAO productDAO;
    private final LanguageDAO languageDAO;
    private final ProductCategoryDAO categoryDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.languageDAO = new LanguageDAO();
        this.categoryDAO = new ProductCategoryDAO();
    }

    // --- DEMO 1: XEM SẢN PHẨM ĐA NGÔN NGỮ ---

    /**
     * TÍNH NĂNG 1: Lấy danh sách sản phẩm đã được dịch cho giao diện người dùng.
     */
    public List<ProductDisplayDTO> getProductsForDisplay(String langCode) {
        // Lấy tất cả các bản dịch cho ngôn ngữ yêu cầu
        List<ProductTranslation> translations = productDAO.getProductsByLanguage(langCode);

        // Chuyển đổi kết quả thành DTO
        return translations.stream()
                .map(pt -> {
                    Product p = pt.getProduct();

                    // Lấy tên Category tương ứng
                    String categoryName = getCategoryNameWithFallback(p.getCategory().getProductCategoryId(), langCode);

                    // Sử dụng constructor 4 tham số đã thống nhất
                    return new ProductDisplayDTO(
                            p.getProductId(),
                            pt.getProductName(),
                            p.getPrice(),
                            categoryName
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * TÍNH NĂNG 2: Xem chi tiết sản phẩm đa ngôn ngữ (bao gồm Mô tả).
     */
    public Optional<ProductDisplayDTO> getProductDetail(Integer productId, String langCode) {
        // Lấy bản dịch cụ thể
        Optional<ProductTranslation> translationOpt = productDAO.getTranslationByIdAndLang(productId, langCode);

        if (translationOpt.isEmpty()) {
            return Optional.empty();
        }

        ProductTranslation pt = translationOpt.get();
        Product p = pt.getProduct();

        // Lấy tên Category tương ứng
        String categoryName = getCategoryNameWithFallback(p.getCategory().getProductCategoryId(), langCode);

        // Tạo DTO chi tiết
        ProductDisplayDTO dto = new ProductDisplayDTO(
                p.getProductId(),
                pt.getProductName(),
                p.getPrice(),
                categoryName
        );
        dto.setProductDescription(pt.getProductDescription());

        return Optional.of(dto);
    }

    // --- DEMO 2: QUẢN LÝ BẢN DỊCH (ADMIN) ---

    /**
     * TÍNH NĂNG 1: Thêm/Cập nhật bản dịch cho một sản phẩm.
     */
    public void saveOrUpdateTranslation(Integer productId, String langCode, String productName, String productDescription) {

        // 1. Lấy Entity gốc và Language Entity (DAO tự quản lý Session)
        // **LƯU Ý:** Nếu MainApp không flush/commit trước đó, productDAO.getProductById(productId) sẽ trả về rỗng!
        Optional<Product> productOpt = productDAO.getProductById(productId);
        Language language = languageDAO.getByCode(langCode);

        // Dòng kiểm tra này là nguyên nhân gây ra lỗi Runtime nếu MainApp chưa flush/commit!
        if (productOpt.isEmpty() || language == null) {
            throw new IllegalArgumentException("Product ID hoặc Language Code không hợp lệ.");
        }

        Product product = productOpt.get();

        // 2. Kiểm tra bản dịch đã tồn tại hay chưa
        Optional<ProductTranslation> existingTranslationOpt = productDAO.getTranslationByIdAndLang(productId, langCode);

        ProductTranslation translation;
        if (existingTranslationOpt.isPresent()) {
            // Cập nhật bản dịch đã tồn tại
            translation = existingTranslationOpt.get();
        } else {
            // Tạo bản dịch mới (sử dụng constructor)
            translation = new ProductTranslation(product, language, productName, productDescription);
        }

        // 3. Cập nhật dữ liệu
        translation.setProductName(productName);
        translation.setProductDescription(productDescription);

        // 4. Lưu/Merge vào CSDL (DAO tự quản lý Session)
        productDAO.saveTranslation(translation);
    }

    /**
     * TÍNH NĂNG 2: Thêm ngôn ngữ mới.
     */
    public void addNewLanguage(String code, String name) {
        if (languageDAO.getByCode(code) != null) {
            throw new IllegalArgumentException("Mã ngôn ngữ " + code + " đã tồn tại.");
        }

        // Sử dụng constructor 2 tham số (đã được thêm vào Language.java)
        Language newLang = new Language(code, name);
        languageDAO.saveLanguage(newLang);
    }

    // --- PHƯƠNG THỨC HỖ TRỢ ---

    /**
     * Phương thức helper để lấy tên danh mục đã dịch (dùng cho DTO).
     */
    private String getCategoryNameWithFallback(Integer categoryId, String langCode) {

        // 1. Tìm bản dịch theo ngôn ngữ yêu cầu
        Optional<String> categoryNameOpt = categoryDAO.getCategoryNameByLanguage(categoryId, langCode);

        if (categoryNameOpt.isPresent()) {
            return categoryNameOpt.get();
        }

        // 2. Fallback về Tiếng Anh (en)
        return categoryDAO.getCategoryNameByLanguage(categoryId, "en")
                .orElse("Category Name Missing");
    }

    /**
     * Phương thức hỗ trợ để lấy danh sách các ngôn ngữ đang hoạt động.
     */
    public List<Language> getAllActiveLanguages() {
        return languageDAO.getAllLanguages();
    }
}