package com.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class ProductRepository {

    private final SessionFactory sessionFactory;

    public ProductRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Product create(Product product) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(product);
            session.getTransaction().commit();
            return product;
        }
    }

    public Product findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Product.class, id);
        }
    }

    public List<Product> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Product", Product.class).getResultList();
        }
    }

    public Product update(int id, Product updated) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Product existing = session.get(Product.class, id);
            if (existing == null) {
                session.getTransaction().rollback();
                return null;
            }
            existing.setName(updated.getName());
            existing.setCategory(updated.getCategory());
            existing.setPrice(updated.getPrice());
            session.merge(existing);
            session.getTransaction().commit();
            return existing;
        }
    }

    public boolean delete(int id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Product existing = session.get(Product.class, id);
            if (existing == null) {
                session.getTransaction().rollback();
                return false;
            }
            session.remove(existing);
            session.getTransaction().commit();
            return true;
        }
    }
}
