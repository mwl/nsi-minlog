package dk.nsi.minlog.export.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dk.sdsd.nsp.slalog.api.SLALogger;
import dk.sdsd.nsp.slalog.api.SLALogConfig;

@Configuration
public class SLALoggerConfig {
	@Bean
	public SLALogger slaLogger(){
		return new SLALogConfig("minlog","minlog").getSLALogger();
	}
}