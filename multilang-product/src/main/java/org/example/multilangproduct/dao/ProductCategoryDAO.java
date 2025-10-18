package org.example.multilangproduct.dao;

import org.example.multilangproduct.config.HibernateUtil;
import org.example.multilangproduct.entity.ProductCategory;
import org.example.multilangproduct.entity.ProductCategoryTranslation;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class ProductCategoryDAO {

    // --- Các phương thức dùng cho MainApp (Nhận Session) ---

    public void saveCategory(Session session, ProductCategory category) {
        session.persist(category);
    }

    public Optional<ProductCategory> getById(Session session, Integer categoryId) {
        return Optional.ofNullable(session.get(ProductCategory.class, categoryId));
    }

    public void saveTranslation(Session session, ProductCategoryTranslation translation) {
        session.merge(translation);
    }

    // --- Các phương thức dùng cho UI/Service (Tự quản lý Session) ---

    /**
     * Lưu Category mới (tự quản lý Session).
     */
    public void saveCategory(ProductCategory category) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(category);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Lỗi khi lưu ProductCategory: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu ProductCategory", e);
        }
    }

    /**
     * Lấy Category theo ID (tự quản lý Session - dùng cho UI).
     */
    public Optional<ProductCategory> getById(Integer categoryId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(ProductCategory.class, categoryId));
        }
    }

    /**
     * Lưu Bản dịch (tự quản lý Session - dùng cho UI).
     */
    public void saveTranslation(ProductCategoryTranslation translation) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(translation);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Lỗi khi lưu Translation Danh mục: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu Translation Danh mục", e);
        }
    }

    /**
     * Lấy tên Danh mục đã dịch theo ID và mã ngôn ngữ (tự quản lý Session).
     */
    public Optional<String> getCategoryNameByLanguage(Integer categoryId, String langCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT pct.categoryName FROM ProductCategoryTranslation pct " +
                    "WHERE pct.category.productCategoryId = :categoryId " +
                    "AND pct.language.code = :langCode";

            Query<String> query = session.createQuery(hql, String.class);
            query.setParameter("categoryId", categoryId);
            query.setParameter("langCode", langCode);

            return query.uniqueResultOptional();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên Danh mục ID:" + categoryId + " bằng ngôn ngữ " + langCode + ". Lỗi: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lấy tất cả các Danh mục sản phẩm (tự quản lý Session - dùng cho UI).
     */
    public List<ProductCategory> getAllCategories() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ProductCategory", ProductCategory.class).list();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả Danh mục. Lỗi: " + e.getMessage());
            return List.of();
        }
    }
}