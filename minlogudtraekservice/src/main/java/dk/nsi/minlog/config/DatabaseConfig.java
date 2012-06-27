package dk.nsi.minlog.config;

import java.util.ArrayList;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.springsupport.factory.EbeanServerFactoryBean;
import com.avaje.ebean.springsupport.txn.SpringAwareJdbcTransactionManager;

import dk.nsi.minlog.domain.LogEntry;

@Configuration
@EnableTransactionManagement
@ComponentScan({"dk.nsi.minlog.dao.ebean"})
public class DatabaseConfig implements TransactionManagementConfigurer{
	static final Logger logger = Logger.getLogger(DatabaseConfig.class);

    @Value("${jdbc.url}") String url;
    @Value("${jdbc.username}") String username;
    @Value("${jdbc.password}") String password;

    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource(
                url,
                username,
                password
        );
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        return dataSource;
    }
    
    @Bean
    public PlatformTransactionManager txManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return txManager();
    }

    @Bean
    public EbeanServerFactoryBean ebeanServer(DataSource dataSource) throws Exception {
    	logger.debug("Creating ebeanServer with url=" + url + " username=" + username);    		
    	
        final EbeanServerFactoryBean factoryBean = new EbeanServerFactoryBean();
        final ServerConfig serverConfig = new ServerConfig();        
        final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(LogEntry.class);
        
        serverConfig.setName("localhostConfig");
        serverConfig.setClasses(classes);
        serverConfig.setDataSource(dataSource);
        serverConfig.setExternalTransactionManager(new SpringAwareJdbcTransactionManager());
        serverConfig.setNamingConvention(new com.avaje.ebean.config.MatchingNamingConvention());
        factoryBean.setServerConfig(serverConfig);
        return factoryBean;
    }
}