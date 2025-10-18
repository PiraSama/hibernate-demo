package org.example.multilangproduct.dao;

import org.example.multilangproduct.config.HibernateUtil;
import org.example.multilangproduct.entity.Language;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class LanguageDAO {

    /**
     * Lưu Language mới. Dùng cho MainApp (nhận Session)
     */
    public void saveLanguage(Session session, Language lang) {
        session.persist(lang);
    }

    /**
     * Lấy Language theo code. Dùng cho MainApp (nhận Session)
     */
    public Language getByCode(Session session, String code) {
        return session.createQuery("FROM Language l WHERE l.code = :code", Language.class)
                .setParameter("code", code)
                .uniqueResult();
    }

    /**
     * Lưu Language mới. Dùng cho Service/UI (tự quản lý Session)
     */
    public void saveLanguage(Language lang) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(lang);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Lỗi khi lưu Language: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Lấy Language theo code. Dùng cho Service/UI (tự quản lý Session)
     */
    public Language getByCode(String code) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Language l WHERE l.code = :code", Language.class)
                    .setParameter("code", code)
                    .uniqueResult();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy Language theo code: " + code + ". Lỗi: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy tất cả các ngôn ngữ (tự quản lý Session).
     */
    public List<Language> getAllLanguages() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Language", Language.class).list();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả Languages. Lỗi: " + e.getMessage());
            return List.of();
        }
    }
}