package dk.nsi.minlog.web;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.jsp.JspWriter;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IsAliveTest {
	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	DataSource dataSource;
	
	@Mock
	JspWriter out;
	
	/**
	 * Check if we can hit the datasource without the code blowing up
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */	
	@Test
	public void checkAll() throws SQLException, IOException {
		when(dataSource.getConnection().createStatement().executeQuery("SELECT 1").next()).thenReturn(true);
		when(dataSource.getConnection().createStatement().executeQuery("SELECT 1").getInt(1)).thenReturn(1);
		
		IsAlive isAlive = new IsAlive();
		isAlive.dataSource = dataSource;
		
		isAlive.checkAll(out);

		//Success if we do not get an exception
	}
	
	/**
	 * Check if the we get an exception, when the datasource does not answer correctly
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test(expected=RuntimeException.class)
	public void checkAllFail() throws SQLException, IOException {
		when(dataSource.getConnection().createStatement().executeQuery("SELECT 1").next()).thenReturn(false);
		when(dataSource.getConnection().createStatement().executeQuery("SELECT 1").getInt(1)).thenReturn(1);
		
		IsAlive isAlive = new IsAlive();
		isAlive.dataSource = dataSource;
		
		isAlive.checkAll(out);
	}

}
