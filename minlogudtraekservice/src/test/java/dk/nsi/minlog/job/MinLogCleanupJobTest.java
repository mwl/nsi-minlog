package dk.nsi.minlog.job;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import dk.nsi.minlog.job.MinLogCleanupJob;
import dk.nsi.minlog.dao.LogEntryDao;

@RunWith(MockitoJUnitRunner.class)
public class MinLogCleanupJobTest {
	
	@Mock
	LogEntryDao registreringDao;
	
	@InjectMocks
	MinLogCleanupJob job;

	
	/**
	 * Make sure we call the dao on a standard clean up. 
	 */
	@Test
	public void removeExecution() {
		job.cleanup();
		verify(registreringDao).removeBefore((DateTime)any());
	}
	

	/**
	 * Test that we are not allowed to run two concurrent cleanup jobs at the same time.
	 */
	@Test
	public void concurrentCleanupRuns(){
		//Delay the answers so we make sure we only run job one at a time
		doAnswer(new Answer<Object>() {
		     public Object answer(InvocationOnMock invocation) {
		    	 try{ Thread.sleep(100); } catch(Exception e){}
		         return null;
		     }
		})
		.when(registreringDao).removeBefore((DateTime)any());		

		//Start first cleanup in a seperate thread, so we can run second cleanup async.
		new Thread(){
			public void run() {
				job.cleanup();				
			};
		}.start();
		
		job.cleanup();

		//Mockito assumes mocked object methods are only called once
		verify(registreringDao).removeBefore((DateTime)any());
	}
	
	/**
	 * Test that job is not running after exception is throw. This allows us to run the job again.
	 */
	@Test
	public void assumeNotRunningOnException(){
		doThrow(new RuntimeException()).when(registreringDao).removeBefore((DateTime)any());
		job.cleanup();
		assertFalse(job.isRunning());
	}
}