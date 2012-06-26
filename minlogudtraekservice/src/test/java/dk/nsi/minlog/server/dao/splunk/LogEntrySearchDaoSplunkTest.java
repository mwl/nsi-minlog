package dk.nsi.minlog.server.dao.splunk;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dk.nsi.minlog.config.SplunkConfig;
import dk.nsi.minlog.server.dao.LogEntrySearchDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SplunkConfig.class})
@ComponentScan({"dk.nsi.minlog.server"})
public class LogEntrySearchDaoSplunkTest {

	@Inject
	LogEntrySearchDao logEntrySearchDao; 
	
	
	@Test
	public void testFindLogEntries() {
		logEntrySearchDao.findLogEntries();
	}

}
