package dk.nsi.minlog.server.dao.ebean;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlRow;

@RunWith(MockitoJUnitRunner.class)
public class WhitelistCheckerDefaultTest {

	@InjectMocks
	WhitelistCheckerDefault whitelistCheckerDefault = new WhitelistCheckerDefault();
	
	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	EbeanServer ebeanServer;
	
	@Test
	public void  getLegalCvrNumbers() throws Exception{
		SqlRow row = mock(SqlRow.class);
		when(row.getString("legal_cvr")).thenReturn("result");		
		when(ebeanServer.createSqlQuery((String)any()).findSet()).thenReturn(Collections.singleton(row));
				
		Set<String> results = whitelistCheckerDefault.getLegalCvrNumbers("test");
		assertTrue(results.contains("result"));		
	}
}
