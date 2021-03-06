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
package dk.nsi.minlog.ws;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import dk.nsi.minlog._2012._05._24.ListLogStatementsRequest;
import dk.nsi.minlog._2012._05._24.ListLogStatementsResponse;
import dk.nsi.minlog.domain.LogEntry;
import dk.nsi.minlog.ws.dao.LogEntryDao;
import dk.nsi.minlog.ws.ws.MinlogudtraekserviceImpl;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("restriction")
public class MinlogudtraekserviceImplTest {

	@Mock
	LogEntryDao logEntryDao;
	
	@InjectMocks
	MinlogudtraekserviceImpl service;

	private DateTime to = DateTime.now();
	private DateTime from = to.minusYears(2);
	private XMLGregorianCalendar fromXml = new XMLGregorianCalendarImpl(from.toGregorianCalendar()); 
	private XMLGregorianCalendar toXml = new XMLGregorianCalendarImpl(to.toGregorianCalendar()); 

	private final static String[] nameFormat = new String[]{"YDERNUMMER", "PNUMBER", "SHAK", "CVRNUMBER", "COMMUNALNUMBER", "SOR", "OTHER", "UNKNOWN_ORGANIZATION"};

	/**
	 * Create a mock dao with data.
	 */	
	@Before
	public void setupDao(){
		int size = 10;
		LogEntry[] entries = new LogEntry[size];
		
		for(int i = 0; i < 10; i++){
			final long id = i;
			entries[i] = (new LogEntry(){{
				setAnsvarlig("ansvarlig" + id);
				setBruger("bruger" + id);
				setCprNrBorger("1234");
				setHandling("handling" + id);
				setId(id);
				setOrgUsingID(nameFormat[(int)id % nameFormat.length] + ":test" + id);
				setRegKode("regKode" + id);
				setSessionId("abc" + id);
				setSystemName("System" + id);
				setTidspunkt(new DateTime());
			}});
		}	
		
		LogEntry error = (new LogEntry(){{
			setAnsvarlig("ansvarlig");
			setBruger("bruger");
			setCprNrBorger("1234");
			setHandling("handling");
			setId(10l);
			setOrgUsingID("error:test");
			setRegKode("regKode");
			setSessionId("abc");
			setSystemName("System");
			setTidspunkt(new DateTime());
		}});
		
		LogEntry emptyOrg = (new LogEntry(){{
			setAnsvarlig("ansvarlig");
			setBruger("bruger");
			setCprNrBorger("1234");
			setHandling("handling");
			setId(10l);
			setOrgUsingID("");
			setRegKode("regKode");
			setSessionId("abc");
			setSystemName("System");
			setTidspunkt(new DateTime());
		}});
		
		when(logEntryDao.findByCPRAndDates(eq("1234"), (DateTime)isNull(), (DateTime)isNull())).thenReturn(asList(new LogEntry[]{
			entries[0], entries[1]
		}));
	
		when(logEntryDao.findByCPRAndDates(eq("1234"), (DateTime)any(), (DateTime)isNull())).thenReturn(asList(new LogEntry[]{
				entries[2], entries[3]
		}));

		when(logEntryDao.findByCPRAndDates(eq("1234"), (DateTime)isNull(), (DateTime)any())).thenReturn(asList(new LogEntry[]{
				entries[4], entries[5]
		}));

		when(logEntryDao.findByCPRAndDates(eq("1234"), (DateTime)notNull(), (DateTime)notNull())).thenReturn(asList(new LogEntry[]{
				entries[6], entries[7]
		}));
		
		when(logEntryDao.findByCPRAndDates(eq("error"), (DateTime)any(), (DateTime)any())).thenReturn(asList(new LogEntry[]{
				error
		}));
		
		when(logEntryDao.findByCPRAndDates(eq("emptyOrg"), (DateTime)any(), (DateTime)any())).thenReturn(asList(new LogEntry[]{
				emptyOrg
		}));
	}
	
	/**
	 * Check if the cpr is attach to the response header
	 */
	
	@Test
	public void cpr() {
		ListLogStatementsRequest request = new ListLogStatementsRequest();
		request.setCprNR("1234");
		
		ListLogStatementsResponse response = service.listLogStatements(request, null);
		assertEquals("1234", response.getCprNrBorger());
	}
	
	
	/**
	 * Check if we can get more then one response row.	
	 */
	@Test
	public void multipleEntries() {
		ListLogStatementsRequest request = new ListLogStatementsRequest();
		request.setCprNR("1234");
		
		ListLogStatementsResponse response = service.listLogStatements(request, null);
		assertEquals(2, response.getLogEntry().size());
	}
	
	/**
	 * Check if we get the correct entries if we specify a from date.
	 */	
	@Test
	public void fromDate() {
		ListLogStatementsRequest request = new ListLogStatementsRequest();
		request.setCprNR("1234");
		request.setFraDato(fromXml);		
		ListLogStatementsResponse response = service.listLogStatements(request, null);
		assertEquals("bruger2", response.getLogEntry().get(0).getBruger());
		assertEquals("bruger3", response.getLogEntry().get(1).getBruger());
	}	

	/**
	 * Check if we get the correct entries if we specify a to date.
	 */
	@Test
	public void toDate() {
		ListLogStatementsRequest request = new ListLogStatementsRequest();
		request.setCprNR("1234");
		request.setTilDato(toXml);
		ListLogStatementsResponse response = service.listLogStatements(request, null);
		assertEquals("bruger4", response.getLogEntry().get(0).getBruger());
		assertEquals("bruger5", response.getLogEntry().get(1).getBruger());
	}	

	/**
	 * Check if we get the correct entries if we specify a from date and a to date.
	 */
	@Test
	public void bothDate() {
		ListLogStatementsRequest request = new ListLogStatementsRequest();
		request.setCprNR("1234");		
		request.setFraDato(fromXml);
		request.setTilDato(toXml);
		ListLogStatementsResponse response = service.listLogStatements(request, null);
		assertEquals("bruger6", response.getLogEntry().get(0).getBruger());
		assertEquals("bruger7", response.getLogEntry().get(1).getBruger());
	}

	@Test
	public void emptyOrganisation() {
		ListLogStatementsRequest request = new ListLogStatementsRequest();
		request.setCprNR("emptyOrg");		
		ListLogStatementsResponse response = service.listLogStatements(request, null);
		assertNull(null, response.getLogEntry().get(0).getBrugerOrganisation());
	}
	
	/**
	 * Check if we the service throws an exception when the requested data is invalid.
	 */
	@Test(expected=RuntimeException.class)
	public void formatError() {
		ListLogStatementsRequest request = new ListLogStatementsRequest();
		request.setCprNR("error");
		service.listLogStatements(request, null);		
	}
}