package dk.nsi.minlog.server.dao.splunk;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import com.splunk.Job;
import com.splunk.Service;

import dk.nsi.minlog.config.ApplicationRootConfig;
import dk.nsi.minlog.domain.LogEntry;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = {ApplicationRootConfig.class})
public class LogEntrySearchDaoSplunkTest {

	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	Service splunkService;
	
	@InjectMocks
	LogEntrySearchDaoSplunk logEntrySearchDao; 
	
	/**
	 * Check if we the parsing of the splunk result is working.
	 * 
	 * Uses a file to simulate the query from splunk. 
	 * 
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("rawtypes")
	public void findLogEntries() throws Exception {
		logEntrySearchDao.sleep = 20;
		
		Job job = splunkService.getJobs().create((String)any());
		InputStream stream = ClassLoader.class.getResourceAsStream("/splunk/queryResult.xml");
		when(job.getResults((Map)any())).thenReturn(stream);
		when(job.isDone()).thenReturn(false, true);
		
		//Mock does not care about dateTime
		List<LogEntry> result = logEntrySearchDao.findLogEntries(DateTime.now(), DateTime.now());
		assertTrue(result.size() == 1);
		
		LogEntry log = result.get(0);
		
		//Test if we have a correct pattern of UUID
		Pattern pattern = Pattern.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
		Matcher match = pattern.matcher(log.getRegKode());		
		assertTrue(match.matches());
		
		assertEquals("1111119999", log.getCprNrBorger());
		assertEquals("0101001000", log.getBruger());
		assertEquals("1111001000", log.getAnsvarlig());
		assertEquals("SOR:12345678", log.getOrgUsingID());
		assertEquals("System name", log.getSystemName());
		assertEquals("Opslag på NPI", log.getHandling());
		assertEquals("urn:uuid:145d6c9c-b873-47ab-9203-8eaa306013ad", log.getSessionId());
		
		DateTime dt = new DateTime(2012, 6, 25, 13, 38,10, 109,  DateTimeZone.UTC);
		assertEquals(dt, log.getTidspunkt());
	}
}