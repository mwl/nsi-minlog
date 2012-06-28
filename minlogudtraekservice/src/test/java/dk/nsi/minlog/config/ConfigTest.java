package dk.nsi.minlog.config;

import static org.mockito.Mockito.*;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.springsupport.factory.EbeanServerFactoryBean;
import com.splunk.Service;

/**
 * Simple test to see if spring is correctly setup, and all dependecies are met.
 *
 * Notice this demands some mocking as we do not want to setup alot of external resources.
 * 
 * @author kpi
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ApplicationRootConfig.class, ConfigTest.DatabaseConfigConfigTest.class, ConfigTest.WSConfigTest.class, ConfigTest.SplunkConfigTest.class})
public class ConfigTest {

	@BeforeClass
	public static void setup(){
		System.setProperty("sosi.production", "false");
	}
			
	@Test
	public void test() {
		//Test is acually empty, because its a success if the setup does not blow up.
	}
	
	@Configuration
	@EnableTransactionManagement
	public static class DatabaseConfigConfigTest extends DatabaseConfig{
		
		@Bean
		@Override
		public DataSource dataSource(){
			return mock(DataSource.class);
		}

		
		@Override
		public EbeanServerFactoryBean ebeanServer(DataSource dataSource) throws Exception {
			return mock(EbeanServerFactoryBean.class);
		}		
	}

	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	public static class WSConfigTest extends WSConfig{
		@Bean
		public EbeanServer ebeanServer(){
			return mock(EbeanServer.class);
		}
	}
	
	@Configuration
	public static class SplunkConfigTest extends SplunkConfig{
		
		@Override
		public Service splunkService(){
			return mock(Service.class);
		}
	}
	
	
}
