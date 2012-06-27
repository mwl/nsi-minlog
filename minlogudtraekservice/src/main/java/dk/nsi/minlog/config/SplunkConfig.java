package dk.nsi.minlog.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.splunk.Service;

@Configuration
@ComponentScan({"dk.nsi.minlog.server.dao.splunk"})
public class SplunkConfig {
	@Value("${minlog.splunk.host}")
	String host;

	@Value("${minlog.splunk.port}")
	Integer port;
	
	@Value("${minlog.splunk.user}")
	String user;
	
	@Value("${minlog.splunk.password}")
	String password;
	
	@Value("${minlog.splunk.schema}")
	String schema;

	
	
	@Bean
	public Service splunkService(){
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("host", host);
		args.put("port", port);
		args.put("schema", schema);
		
		
		Service service = new Service(args);
		service.login(user, password);
		
		return service;
	}
}