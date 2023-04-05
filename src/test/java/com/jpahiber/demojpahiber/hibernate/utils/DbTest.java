package com.jpahiber.demojpahiber.hibernate.utils;

import com.jpahiber.demojpahiber.config.enums.DataSourceProxyType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

@Slf4j
public abstract class DbTest {

    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws IOException {
        sessionFactory = getSessionFactory();
        afterInit();
    }

    @After
    public void destroy() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    protected void afterInit() {
    }

    protected void doInJPA(Consumer<EntityManager> function) {
        log.info("\n\n");
        startTransaction(function);
    }

    protected DataSourceProxyType getDatasourceProxyType() {
        return null;
    }

    protected abstract Class<?>[] entities();

    protected Properties getCustomProperties() {
        return null;
    }

    private void startTransaction(Consumer<EntityManager> function) {
        EntityManager entityManager = null;
        EntityTransaction txn = null;
        try {

            entityManager = sessionFactory.createEntityManager();
            txn = entityManager.getTransaction();
            txn.begin();
            function.accept(entityManager);
            if (!txn.getRollbackOnly()) {
                txn.commit();
            } else {
                try {
                    txn.rollback();
                } catch (Exception e) {
                    log.error("Rollback failure", e);
                }
            }
        } catch (Throwable t) {
            if (txn != null && txn.isActive()) {
                try {
                    txn.rollback();
                } catch (Exception e) {
                    log.error("Rollback failure", e);
                }
            }
            throw t;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    private SessionFactory getSessionFactory() throws IOException {
        LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();


        localSessionFactoryBean.setDataSource(buildDataSource());
        localSessionFactoryBean.setAnnotatedClasses(entities());
        localSessionFactoryBean.setHibernateProperties(getProps());
        localSessionFactoryBean.afterPropertiesSet();
        return localSessionFactoryBean.getObject();
    }

    private DataSource buildDataSource() {
        DataSourceProxyType dspt = getDatasourceProxyType();
        if (dspt == null) {
            dspt = DataSourceProxyType.DATA_SOURCE_PROXY;
        }

        return dspt.dataSource(DataSourceBuilder.create()
                .username("relationship")
                .password("password")
                .url("jdbc:mysql://127.0.0.1:3306/relationship?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC")
                .build());
    }

    private Properties getProps() {
        Properties properties = new Properties();
        properties.put("hibernate.hbm2ddl.auto", "create");
        properties.put("hibernate.connection.autocommit", true);
        if (getCustomProperties() !=null && !getCustomProperties().isEmpty()){
            properties.putAll(getCustomProperties());
        }

        return properties;
    }
}
