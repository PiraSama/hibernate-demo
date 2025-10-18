package org.example.multilangproduct;

import org.example.multilangproduct.config.HibernateUtil;
import org.example.multilangproduct.dao.LanguageDAO;
import org.example.multilangproduct.dao.ProductCategoryDAO;
import org.example.multilangproduct.dao.ProductDAO;
import org.example.multilangproduct.entity.Language;
import org.example.multilangproduct.entity.Product;
import org.example.multilangproduct.entity.ProductCategory;
import org.example.multilangproduct.entity.ProductCategoryTranslation;
import org.example.multilangproduct.service.ProductService;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.Optional;

public class MainApp {

    private static final LanguageDAO languageDAO = new LanguageDAO();
    private static final ProductDAO productDAO = new ProductDAO();
    private static final ProductCategoryDAO categoryDAO = new ProductCategoryDAO();
    private static final ProductService productService = new ProductService();

    public static void main(String[] args) {

        Session session = null;
        Transaction tx = null;
        Integer demoProductId = 1; // ID mặc định cho lần kiểm tra

        try {
            System.out.println("=== 1. Chuẩn bị Dữ liệu Ngôn ngữ và Danh mục ===");

            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // 1. Tạo ngôn ngữ
            Language enLang = createOrGetLanguage(session, "en", "English", "EN");
            Language vnLang = createOrGetLanguage(session, "vi", "Tiếng Việt", "VI");

            // 2. Tạo Danh mục
            ProductCategory electronicsCategory = createOrGetCategory(session, 1);
            createOrUpdateCategoryTranslation(session, electronicsCategory, enLang, "Electronics");
            createOrUpdateCategoryTranslation(session, electronicsCategory, vnLang, "Thiết bị Điện tử");

            // 3. Tạo Sản phẩm mẫu
            System.out.println("\n=== 2. Tạo Sản phẩm và Bản dịch ===");

            Optional<Product> productOpt = productDAO.getProductById(session, demoProductId);

            if (productOpt.isEmpty()) {
                Product product = new Product(BigDecimal.valueOf(999.99), BigDecimal.valueOf(1.5), electronicsCategory);
                productDAO.saveProduct(session, product);

                // Lấy ID thực tế được gán
                demoProductId = product.getProductId();
                System.out.println("Tạo Product ID:" + demoProductId + " thành công!");

                // *** SỬA LỖI CUỐI CÙNG ***
                // COMMIT Transaction để Product ID được ghi vĩnh viễn vào CSDL
                // và có thể được ProductService tìm thấy trong Session riêng.
                tx.commit();
                session.close(); // Đóng session đã commit
                session = null;
                tx = null;
                // *** KẾT THÚC SỬA LỖI ***

                // 4. Tạo Translation (ProductService sẽ tự quản lý Session của nó)
                productService.saveOrUpdateTranslation(demoProductId, "en", "High-End Laptop", "A powerful laptop for professionals.");
                productService.saveOrUpdateTranslation(demoProductId, "vi", "Máy tính xách tay Cao cấp", "Máy tính mạnh mẽ dành cho dân chuyên nghiệp.");
                System.out.println("=== Khởi tạo Sản phẩm và Bản dịch hoàn tất ===");

            } else {
                Product product = productOpt.get();
                demoProductId = product.getProductId();
                System.out.println("Product ID:" + demoProductId + " đã tồn tại, bỏ qua.");
                // Commit và đóng Transaction ngay cả khi không có gì thay đổi
                tx.commit();
                session.close();
                session = null;
                tx = null;
                System.out.println("=== Khởi tạo CSDL hoàn tất ===");
            }

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            System.err.println("\nLỖI KHỞI TẠO CSDL: Đã thực hiện rollback.");
        } finally {
            // Đảm bảo session không bị đóng lại nếu đã đóng ở trên
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        // --- Phần Test Demo 1 ---
        System.out.println("\n=== 3. Test Demo 1: Hiển thị Sản phẩm ===");
        System.out.println("--- Hiển thị Tiếng Anh (EN) ---");
        productService.getProductsForDisplay("en").forEach(dto ->
                System.out.printf("ID: %d, Tên: %s, Giá: %.2f, Danh mục: %s\n",
                        dto.getProductId(), dto.getProductName(), dto.getPrice(), dto.getCategoryName())
        );
    }

    // --- Phương thức Helper (Giữ nguyên) ---

    private static Language createOrGetLanguage(Session session, String code, String name, String id) {
        Language lang = languageDAO.getByCode(session, code);
        if (lang == null) {
            Language newLang = new Language(id, name, code);
            languageDAO.saveLanguage(session, newLang);
            System.out.println("Đã thêm ngôn ngữ: " + name);
            return newLang;
        }
        return lang;
    }

    private static ProductCategory createOrGetCategory(Session session, Integer id) {
        Optional<ProductCategory> categoryOpt = categoryDAO.getById(session, id);
        if (categoryOpt.isEmpty()) {
            ProductCategory category = new ProductCategory(true);
            categoryDAO.saveCategory(session, category);
            System.out.println("Tạo Category ID:" + category.getProductCategoryId() + " thành công!");
            return category;
        }
        return categoryOpt.get();
    }

    private static void createOrUpdateCategoryTranslation(Session session, ProductCategory category, Language lang, String name) {
        ProductCategoryTranslation translation = new ProductCategoryTranslation(category, lang, name);
        categoryDAO.saveTranslation(session, translation);
    }
}