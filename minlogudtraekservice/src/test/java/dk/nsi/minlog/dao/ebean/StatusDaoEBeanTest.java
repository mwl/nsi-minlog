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
package dk.nsi.minlog.dao.ebean;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlRow;

import dk.nsi.minlog.dao.ebean.StatusDaoEBean;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class StatusDaoEBeanTest {		
	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	EbeanServer ebeanServer;
	
	@InjectMocks
	StatusDaoEBean statusDao = new StatusDaoEBean();
	
	@Test
	public void fetchingStatus() throws Exception {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		SqlRow row = mock(SqlRow.class); 
		when(row.getTimestamp("lastUpdated")).thenReturn(ts);
		when(ebeanServer.createSqlQuery((String)any()).findUnique()).thenReturn(row);
		
		DateTime expectedDate = new DateTime(ts.getTime());
		
		assertEquals(expectedDate, statusDao.getLastUpdated());				
	}
	
	@Test
	public void setStatus() throws Exception {
		statusDao.setLastUpdated(DateTime.now());
		verify(ebeanServer).createSqlUpdate((String)any());
	}
}
