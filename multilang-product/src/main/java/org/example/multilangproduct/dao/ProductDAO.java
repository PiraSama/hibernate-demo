package org.example.multilangproduct.dao;

import org.example.multilangproduct.config.HibernateUtil;
import org.example.multilangproduct.entity.Product;
import org.example.multilangproduct.entity.ProductTranslation;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    // --- Các phương thức dùng cho MainApp (Nhận Session) ---

    public void saveProduct(Session session, Product product) {
        session.persist(product);
    }

    public Optional<Product> getProductById(Session session, Integer productId) {
        return Optional.ofNullable(session.get(Product.class, productId));
    }

    // --- Các phương thức dùng cho UI/Service (Tự quản lý Session) ---

    // Thêm sản phẩm (gốc) - Tự quản lý Session (dùng cho UI)
    public void saveProduct(Product product) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(product);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu Product: " + e.getMessage(), e);
        }
    }

    public List<ProductTranslation> getProductsByLanguage(String langCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT pt FROM ProductTranslation pt " +
                                    "JOIN FETCH pt.product p " +
                                    "WHERE pt.language.code = :langCode " +
                                    "ORDER BY p.productId", ProductTranslation.class)
                    .setParameter("langCode", langCode)
                    .getResultList();
        }
    }

    // Lưu bản dịch sản phẩm - Tự quản lý Session (dùng cho Service)
    public void saveTranslation(ProductTranslation translation) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(translation);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Lỗi save Translation: " + e.getMessage(), e);
        }
    }

    // Lấy sản phẩm theo ID - Tự quản lý Session (dùng cho UI)
    public Optional<Product> getProductById(Integer productId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Product.class, productId));
        }
    }

    // ... (Các phương thức khác tự quản lý Session) ...

    public Optional<String> getProductNameByLanguage(Integer productId, String langCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT pt.productName FROM ProductTranslation pt " +
                                    "WHERE pt.product.productId = :productId AND pt.language.code = :langCode", String.class)
                    .setParameter("productId", productId)
                    .setParameter("langCode", langCode)
                    .uniqueResultOptional();
        }
    }

    public Optional<ProductTranslation> getTranslationByIdAndLang(Integer productId, String langCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ProductTranslation> query = session.createQuery(
                    "SELECT pt FROM ProductTranslation pt " +
                            "WHERE pt.product.productId = :productId AND pt.language.code = :langCode", ProductTranslation.class);

            query.setParameter("productId", productId);
            query.setParameter("langCode", langCode);

            // Sử dụng uniqueResultOptional để xử lý trường hợp không tìm thấy
            return query.uniqueResultOptional();
        }
    }

    // Xóa sản phẩm và translations liên quan - Tự quản lý Session (dùng cho UI)
    public void deleteProduct(Integer productId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Product product = session.get(Product.class, productId);
            if (product != null) {
                session.remove(product);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xóa sản phẩm", e);
        }
    }

    public void updateTranslation(ProductTranslation translation) {
        saveTranslation(translation);
    }
}