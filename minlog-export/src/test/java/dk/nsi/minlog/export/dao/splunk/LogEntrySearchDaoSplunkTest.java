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
package dk.nsi.minlog.export.dao.splunk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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

import com.splunk.Job;
import com.splunk.Service;

import dk.nsi.minlog.export.domain.LogEntry;

@RunWith(MockitoJUnitRunner.class)
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