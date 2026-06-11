package com.ambulatorio.database;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JpaUtil {

    private static JpaUtil instance;
    private EntityManagerFactory emf;

    private JpaUtil() {
        Map<String, String> properties = new HashMap<>();

        Dotenv dotenv = Dotenv.load();
        String dbName = dotenv.get("DB_NAME");
        String dbUser = dotenv.get("DB_USER");
        String dbPassword = dotenv.get("DB_PASSWORD");
        String dbUrl = "jdbc:mysql://127.0.0.1:3306/" + dbName;

        properties.put("jakarta.persistence.jdbc.url", dbUrl);
        properties.put("jakarta.persistence.jdbc.user", dbUser);
        properties.put("jakarta.persistence.jdbc.password", dbPassword);

        emf = Persistence.createEntityManagerFactory("ambulatorioPU", properties);
    }

    public static JpaUtil getInstance() {
        if (instance == null) {
            instance = new JpaUtil();
        }
        return instance;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void chiudi() {
        emf.close();
    }
}
