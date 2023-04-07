package com.jpahiber.demojpahiber.config.providers;


import java.util.Optional;
import java.util.Properties;

public class MySQLDataSourceProvider extends DataSourceProvider {

    @Override
    public Optional<Properties> dataSourceProperties() {
        return Optional.of(new Properties(){{
            put(HIBERNATE_DIALECT, dialect());
        }});
    }

    @Override
    public Optional<String> propertiesFile() {
        return Optional.of("mysql.yml");
    }
}
