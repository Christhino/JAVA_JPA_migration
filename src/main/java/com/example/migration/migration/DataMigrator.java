package com.example.migration.migration;

import com.example.migration.entity.TableXEntity;
import com.example.migration.entity.User;
import com.example.migration.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

import java.util.List;

public class DataMigrator {
    public static void migrateUsers() {
        Session sourceSession = HibernateUtil.getSourceSession();
        Session destinationSession = HibernateUtil.getDestinationSession();

        Transaction transaction = null;

        try {
            transaction = destinationSession.beginTransaction();

            List<User> users = sourceSession.createQuery("FROM User", User.class).list();


            for (User user : users ) {
                destinationSession.save(user);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            sourceSession.close();
            destinationSession.close();
        }
    }
    public static void copyTable(String sourceTableName, String destinationTableName) {
        Session sourceSession = HibernateUtil.getSourceSession();
        Session destinationSession = HibernateUtil.getDestinationSession();
        Transaction transaction = null;

        try {
            transaction = destinationSession.beginTransaction();

            // Vérifier si la table de destination existe déjà
            NativeQuery<Object> checkTableQuery = destinationSession.createNativeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                            "WHERE table_schema = DATABASE() AND table_name = :tableName");
            checkTableQuery.setParameter("tableName", destinationTableName);
            int tableExists = ((Number) checkTableQuery.getSingleResult()).intValue();

            if (tableExists > 0) {
                System.out.println("Table '" + destinationTableName + "' already exists. Skipping table creation.");
            } else {
                // Récupérer la structure de la table source depuis les informations du catalogue PostgreSQL
                String columnQuery = "SELECT column_name, data_type, is_nullable, column_default " +
                        "FROM information_schema.columns " +
                        "WHERE table_name = :tableName";

                NativeQuery<Object[]> columnInfoQuery = sourceSession.createNativeQuery(columnQuery);
                columnInfoQuery.setParameter("tableName", sourceTableName);
                List<Object[]> tableStructure = columnInfoQuery.getResultList();

                // Créer la table de destination
                StringBuilder createTableQuery = new StringBuilder("CREATE TABLE " + destinationTableName + " (");
                for (Object[] column : tableStructure) {
                    String columnName = (String) column[0];
                    String dataType = (String) column[1];
                    String isNullable = (String) column[2];
                    String columnDefault = (String) column[3];

                    createTableQuery.append(columnName).append(" ");

                    // Conversion des types de données pour MySQL
                    switch (dataType) {
                        case "character varying":
                            createTableQuery.append("VARCHAR(255)"); // Taille 255 comme exemple, ajustez selon votre besoin
                            break;
                        case "bigint":
                            createTableQuery.append("BIGINT");
                            break;
                        // Ajoutez des cas pour d'autres types de données nécessaires
                        default:
                            createTableQuery.append(dataType);
                            break;
                    }

                    if ("NO".equalsIgnoreCase(isNullable)) {
                        createTableQuery.append(" NOT NULL");
                    }

                    if (columnDefault != null) {
                        createTableQuery.append(" DEFAULT ").append(columnDefault);
                    }

                    createTableQuery.append(", ");
                }

                createTableQuery.setLength(createTableQuery.length() - 2); // Retirer la dernière virgule
                createTableQuery.append(")");

                destinationSession.createNativeQuery(createTableQuery.toString()).executeUpdate();

                System.out.println("Table '" + destinationTableName + "' created successfully.");
            }

            // Copier les données
            List<Object[]> sourceData = sourceSession.createNativeQuery("SELECT * FROM " + sourceTableName).getResultList();
            for (Object[] rowData : sourceData) {
                StringBuilder insertQuery = new StringBuilder("INSERT INTO " + destinationTableName + " VALUES (");
                for (Object value : rowData) {
                    if (value instanceof String || value instanceof Character) {
                        insertQuery.append("'").append(value).append("', ");
                    } else if (value == null) {
                        insertQuery.append("NULL, ");
                    } else {
                        insertQuery.append(value).append(", ");
                    }
                }
                insertQuery.setLength(insertQuery.length() - 2); // Retirer la dernière virgule
                insertQuery.append(")");
                destinationSession.createNativeQuery(insertQuery.toString()).executeUpdate();
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            sourceSession.close();
            destinationSession.close();
        }
    }

    public static void changeFieldType(String tableName, String fieldName, String newType) {
        Session session = HibernateUtil.getDestinationSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            String alterQuery = "ALTER TABLE " + tableName + " MODIFY COLUMN " + fieldName + " " + newType;
            session.createNativeQuery(alterQuery).executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public static void insertDataWithTranslation() {
        Session sourceSession = HibernateUtil.getSourceSession();
        Session destinationSession = HibernateUtil.getDestinationSession();
        Transaction transaction = null;

        try {
            transaction = destinationSession.beginTransaction();
            List<User> users = sourceSession.createQuery("FROM User", User.class).list();
            for (User user : users) {
                user.setUsername(user.getUsername().toUpperCase());
                destinationSession.save(user);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            sourceSession.close();
            destinationSession.close();
        }
    }

    public static void main(String[] args) {
        migrateUsers();

        copyTable("users", "PERSON");

        changeFieldType("users", "adress", "int");

        insertDataWithTranslation();
    }
}
