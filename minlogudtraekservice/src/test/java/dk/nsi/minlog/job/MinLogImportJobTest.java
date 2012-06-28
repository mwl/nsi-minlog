package dk.nsi.minlog.job;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import dk.nsi.minlog.dao.LogEntryDao;
import dk.nsi.minlog.dao.StatusDao;
import dk.nsi.minlog.dao.splunk.LogEntrySearchDaoSplunk;
import dk.nsi.minlog.domain.LogEntry;

@RunWith(MockitoJUnitRunner.class)
public class MinLogImportJobTest {
	
	@Mock
	LogEntryDao logEntryDao;
	
	@Mock
	StatusDao statusDao;
	
	@Mock
	LogEntrySearchDaoSplunk logEntrySearchDao;
	
	@InjectMocks
	MinLogImportJob minLogImportJob;
	

	@Test
	public void importJob() {
		minLogImportJob.delay = 10;

		List<LogEntry> entries = Collections.singletonList(new LogEntry());
		when(logEntrySearchDao.findLogEntries((DateTime)any(), (DateTime)any())).thenReturn(entries);
				
		minLogImportJob.startImport();
		
		verify(logEntryDao).save(entries);
	}
}
