package com.todocristal.fabrica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuración principal de la aplicación.
 * Sustituye el antiguo spring-config.xml
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "com.todocristal.fabrica.controller",
    "com.todocristal.fabrica.webservice.dao",
    "com.todocristal.fabrica.webservice.services"
})
public class AppConfig {

    /**
     * Configuración del DataSource.
     * Nota: En producción, usar variables de entorno o archivo externo para credenciales.
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/tff?useSSL=false&serverTimezone=UTC");
        dataSource.setUsername("android");
        dataSource.setPassword("Fabricasion17/");
        return dataSource;
    }

    /**
     * Configuración de Hibernate SessionFactory.
     * Escanea las entidades en el paquete de modelos.
     */
    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("com.todocristal.fabrica.webservice.model");
        
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        hibernateProperties.setProperty("hibernate.show_sql", "true");
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.setProperty("hibernate.format_sql", "true");
        
        sessionFactory.setHibernateProperties(hibernateProperties);
        return sessionFactory;
    }

    /**
     * TransactionManager para управления транзакциями.
     */
    @Bean
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }
}
