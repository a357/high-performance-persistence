package com.jpahiber.demojpahiber.config.enums;

import com.jpahiber.demojpahiber.config.providers.DataSourceProvider;
import com.jpahiber.demojpahiber.config.providers.HSQLDBDataSourceProvider;
import com.jpahiber.demojpahiber.config.providers.MySQLDataSourceProvider;


public enum Database {
    MYSQL(MySQLDataSourceProvider.class),
    HSQLDB(HSQLDBDataSourceProvider.class);

    private Class<? extends DataSourceProvider> dataSourceProvider;

    Database(Class<? extends DataSourceProvider> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    public DataSourceProvider dataSourceProvider() {
        try {
            Class clazz = dataSourceProvider.forName(dataSourceProvider.getName(),false,
                    Thread.currentThread().getContextClassLoader());
            return (DataSourceProvider) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
