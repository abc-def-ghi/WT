package com.test;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {

    private static SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            var cfgUrl = HibernateUtil.class.getClassLoader().getResource("hibernate.cfg.xml");
            if (cfgUrl == null) {
                throw new IllegalStateException("hibernate.cfg.xml not found on classpath");
            }
            sessionFactory = new Configuration()
                .configure(cfgUrl)
                .buildSessionFactory();
        }
        return sessionFactory;
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
