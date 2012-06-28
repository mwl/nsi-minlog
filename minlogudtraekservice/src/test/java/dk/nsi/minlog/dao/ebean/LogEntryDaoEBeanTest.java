package dk.nsi.minlog.dao.ebean;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.avaje.ebean.EbeanServer;

import dk.nsi.minlog.dao.LogEntryDao;
import dk.nsi.minlog.dao.ebean.LogEntryDaoEBean;
import dk.nsi.minlog.domain.LogEntry;



@RunWith(MockitoJUnitRunner.class)
public class LogEntryDaoEBeanTest {

	@InjectMocks
	LogEntryDao logEntryDao = new LogEntryDaoEBean();
	
	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	EbeanServer ebeanServer;
		
	public List<LogEntry> createResult(){
		List<LogEntry> entries = new ArrayList<LogEntry>();
		entries.add(new LogEntry());
		entries.add(new LogEntry());
		entries.add(new LogEntry());		
		return entries;
	}

	/**
	 * Test if we get data from the dao
	 * 
	 * @throws Exception
	 */	
	@Test
	public void findByCpr() throws Exception {			
		List<LogEntry> entries = createResult();
		when(ebeanServer.find(LogEntry.class).where().eq("cprNrBorger", "1234").findList()).thenReturn(entries);
		List<LogEntry> result = logEntryDao.findByCPRAndDates("1234", null, null);
		
		assertTrue(entries.containsAll(result));		
	}

	/**
	 * Test if we get data from the dao if we specify a from date
	 * 
	 * @throws Exception
	 */
	@Test
	public void findByCprAndFrom() throws Exception {
		List<LogEntry> entries = createResult();
		when(ebeanServer.find(LogEntry.class).where().eq("cprNrBorger", "1234").ge(eq("tidspunkt"), any()).findList()).thenReturn(entries);		
		List<LogEntry> result = logEntryDao.findByCPRAndDates("1234", DateTime.now(), null);
		
		assertTrue(entries.containsAll(result));		
	}

	/**
	 * Test if we get data from the dao if we specify a to date
	 * 
	 * @throws Exception
	 */
	@Test
	public void findByCprAndTo() throws Exception {
		List<LogEntry> entries = createResult();
		when(ebeanServer.find(LogEntry.class).where().eq("cprNrBorger", "1234").le(eq("tidspunkt"), any()).findList()).thenReturn(entries);		
		List<LogEntry> result = logEntryDao.findByCPRAndDates("1234", null, DateTime.now());
		
		assertTrue(entries.containsAll(result));		
	}
}