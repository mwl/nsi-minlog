package dk.nsi.minlog.config;

import static java.lang.System.*;

import java.util.ArrayList;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.springsupport.factory.EbeanServerFactoryBean;
import com.avaje.ebean.springsupport.txn.SpringAwareJdbcTransactionManager;

import dk.nsi.minlog.domain.LogEntry;

/**
 * Setup of the application
 *  
 * @author kpi
 * 
 */
@Configuration
@EnableScheduling
@EnableTransactionManagement
@ComponentScan({"dk.nsi.minlog.job"})
public class ApplicationRootConfig {	
    @Bean
    public static PropertyPlaceholderConfigurer configuration() {
        final PropertyPlaceholderConfigurer props = new PropertyPlaceholderConfigurer();
        props.setLocations(new Resource[]{
                new ClassPathResource("default.properties"),
                new FileSystemResource(getProperty("jboss.server.config.url") + "minlog." + getProperty("user.name") + ".properties"),
                new ClassPathResource("minlog." + getProperty("user.name") + ".properties"),
                new ClassPathResource("jdbc.default.properties"),
                new FileSystemResource(getProperty("jboss.server.config.url") + "jdbc." + getProperty("user.name") + ".properties"),
                new ClassPathResource("jdbc." + getProperty("user.name") + ".properties"),
                new FileSystemResource(getProperty("user.home") + "/.minlog/passwords.properties")                
        });
        props.setIgnoreResourceNotFound(true);
        props.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        
        return props;
    }
}
