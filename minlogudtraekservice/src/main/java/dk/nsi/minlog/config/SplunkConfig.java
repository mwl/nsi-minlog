package dk.nsi.minlog.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.splunk.Service;

@Configuration
@ComponentScan({"dk.nsi.minlog.server.dao.splunk"})
public class SplunkConfig {
	
	@Bean
	public Service splunkService(){
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("host", "localhost");
		args.put("port", 8089);
		Service service = new Service(args);
		service.login("admin", "minlog");
		
		return service;
	}
}