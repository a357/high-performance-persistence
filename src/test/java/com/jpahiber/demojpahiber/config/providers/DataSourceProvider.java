package com.jpahiber.demojpahiber.config.providers;

import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public abstract class DataSourceProvider {
    public static final String HIBERNATE_DIALECT = "hibernate.dialect";

    private final Properties properties = new Properties();

    {
        initProperties();
    }

    public DataSource dataSource() {
        return DataSourceBuilder.create().username(username()).password(password()).url(url()).build();
    }

    public String username() {
        return properties.getProperty("db.username");
    }

    public String password() {
        return properties.getProperty("db.password");
    }

    public String url() {
        return properties.getProperty("db.url");
    }

    public String dialect() {
        return properties.getProperty("db.dialect");
    }

    public Optional<Properties> dataSourceProperties() {
        return Optional.empty();
    }

    protected Optional<String> propertiesFile() {
        return Optional.empty();
    }

    private void initProperties() {
        propertiesFile().ifPresent(file -> {
            try (InputStream is = this.getClass().getResourceAsStream(String.format("/providers/%s", file))) {
                properties.load(is);
            } catch (IOException e) {
                throw new RuntimeException("properties not found");
            }
        });
    }
}
