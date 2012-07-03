/**
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dk.nsi.minlog.export.job;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import dk.nsi.minlog.domain.LogEntry;
import dk.nsi.minlog.export.dao.LogEntryDao;
import dk.nsi.minlog.export.dao.StatusDao;
import dk.nsi.minlog.export.dao.splunk.LogEntrySearchDaoSplunk;

@RunWith(MockitoJUnitRunner.class)
public class MinLogImportJobTest {
	
	@Mock
	LogEntrySearchDaoSplunk logEntrySearchDao;
	
	@Mock
	LogEntryDao logEntryDao;
	
	@Mock
	StatusDao statusDao;
		
	@InjectMocks
	MinLogImportJob minLogImportJob;
	

	/**
	 * Test if import fetches from splunk and into the database
	 */
	@Test
	public void importJob() throws Exception{
		minLogImportJob.delay = 10;

		List<LogEntry> entries = Collections.singletonList(new LogEntry());
			
		when(logEntrySearchDao.findLogEntries((DateTime)any(), (DateTime)any())).thenReturn(entries);

		minLogImportJob.startImport();
		
		verify(logEntryDao).save(entries);
	}
	
	/**
	 * Test if import can recover from exception
	 */
	@Test
	public void importJobFail() throws Exception{
		minLogImportJob.delay = 10;

		when(logEntrySearchDao.findLogEntries((DateTime)any(), (DateTime)any())).thenThrow(new RuntimeException());

		minLogImportJob.startImport();
		
		Thread.sleep(minLogImportJob.delay * 10);
		
		assertFalse(minLogImportJob.isRunning());
		
	}
}
