package com.example.migration.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sourceSessionFactory;
    private static final SessionFactory destinationSessionFactory;

    static {
        try {
            sourceSessionFactory = new Configuration().configure("hibernate-source.cfg.xml").buildSessionFactory();
            destinationSessionFactory = new Configuration().configure("hibernate-destination.cfg.xml").buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSourceSession() {
        return sourceSessionFactory.openSession();
    }

    public static Session getDestinationSession() {
        return destinationSessionFactory.openSession();
    }
}
