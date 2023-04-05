package com.jpahiber.demojpahiber.hibernate.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.junit.Before;

import java.util.function.Consumer;

@Log4j2
public abstract class AbstractTest {
    protected EntityManagerFactory emf;


    protected abstract Class<?>[] entities();

    @Before
    public void setUp(){
        emf = buildEntityManagerFactory();
        afterInit();
    }

    protected void afterInit() {}
    protected void doInJPA(Consumer<EntityManager> function) {
        EntityManager entityManager = null;
        EntityTransaction txn = null;
        try {

            entityManager = emf.createEntityManager();
            txn = entityManager.getTransaction();
            txn.begin();
            function.accept(entityManager);
            if ( !txn.getRollbackOnly() ) {
                txn.commit();
            }
            else {
                try {
                    txn.rollback();
                }
                catch (Exception e) {
                    log.error( "Rollback failure", e );
                }
            }
        } catch (Throwable t) {
            if ( txn != null && txn.isActive() ) {
                try {
                    txn.rollback();
                }
                catch (Exception e) {
                    log.error( "Rollback failure", e );
                }
            }
            throw t;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }


    private EntityManagerFactory buildEntityManagerFactory() {
        //create a standard service registry
        BootstrapServiceRegistryBuilder bootstrapServiceRegistryBuilder = new BootstrapServiceRegistryBuilder();
        bootstrapServiceRegistryBuilder.enableAutoClose();
        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder(bootstrapServiceRegistryBuilder.build())
                .applySetting("hibernate.hbm2ddl.auto", "create")
                .applySetting("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect")
                .applySetting("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
                .applySetting("hibernate.connection.username", "relationship")
                .applySetting("hibernate.connection.password", "password")
                .applySetting("hibernate.connection.autocommit", true)
                .applySetting("hibernate.connection.url", "jdbc:mysql://127.0.0.1:3306/relationship?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC")
                .build();

        //create metadata sources
        MetadataSources metadataSources = new MetadataSources(standardServiceRegistry)
                .addAnnotatedClasses(entities());

        //create metadata builder
        MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();

        //create metadata
        MetadataImplementor metadataImplementor = (MetadataImplementor) metadataBuilder.build();

        //create session factory builder
        SessionFactoryBuilder sessionFactoryBuilder = metadataImplementor.getSessionFactoryBuilder();

        //create session factory
        SessionFactory sessionFactory = sessionFactoryBuilder
                .build();

        return sessionFactory;
    }
}
