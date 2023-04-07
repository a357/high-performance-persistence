package com.jpahiber.demojpahiber.config.providers;


import org.hsqldb.jdbc.JDBCDataSource;

import javax.sql.DataSource;
import java.util.Optional;

public class HSQLDBDataSourceProvider extends DataSourceProvider {

    @Override
    public DataSource dataSource() {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl(url());
        dataSource.setUser(username());
        dataSource.setPassword(password());
        return dataSource;
    }

    @Override
    public Optional<String> propertiesFile(){
        return Optional.of("hsqldb.yml");
    }
}
