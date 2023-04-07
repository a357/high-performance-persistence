package com.jpahiber.demojpahiber.config.utils;

import com.jpahiber.demojpahiber.config.enums.DataSourceProxyType;
import com.jpahiber.demojpahiber.config.enums.Database;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Optional;
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

    protected Optional<Properties> getCustomProperties() {
        return Optional.empty();
    }

    protected Database database() {
        return Database.MYSQL;
    }

    private void startTransaction(Consumer<EntityManager> function) {
        EntityTransaction txn = null;
        try (EntityManager entityManager = sessionFactory.createEntityManager()){
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
        return Optional.ofNullable(getDatasourceProxyType())
                .orElse(DataSourceProxyType.DATA_SOURCE_PROXY)
                .dataSource(database().dataSourceProvider().dataSource());
    }

    private Properties getProps() {
        Properties properties = new Properties();
        properties.put("hibernate.hbm2ddl.auto", "create");
        properties.put("hibernate.connection.autocommit", true);

        database().dataSourceProvider().dataSourceProperties()
                .ifPresent(properties::putAll);

        getCustomProperties()
                .ifPresent(properties::putAll);

        return properties;
    }
}
