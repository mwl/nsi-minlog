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
