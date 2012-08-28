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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import dk.nsi.minlog.export.dao.LogEntryDao;
import dk.sdsd.nsp.slalog.api.SLALogger;

@RunWith(MockitoJUnitRunner.class)
public class MinLogCleanupJobTest {
	
	@Mock
	LogEntryDao logEntryDao;

	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	SLALogger slaLogger;

	@InjectMocks
	MinLogCleanupJob job;

	
	/**
	 * Make sure we call the dao on a standard clean up. 
	 * @throws Exception 
	 */
	@Test
	public void removeExecution() throws Exception {
		job.cleanup();
		verify(logEntryDao).removeBefore((DateTime)any());
	}
	

	/**
	 * Test that we are not allowed to run two concurrent cleanup jobs at the same time.
	 */
	@Test
	public void concurrentCleanupRuns() throws Exception{
		//Delay the answers so we make sure we only run job one at a time
		doAnswer(new Answer<Object>() {
		     public Object answer(InvocationOnMock invocation) {
		    	 try{ Thread.sleep(100); } catch(Exception e){}
		         return null;
		     }
		})
		.when(logEntryDao).removeBefore((DateTime)any());		

		//Start first cleanup in a seperate thread, so we can run second cleanup async.
		new Thread(){
			public void run() {
				try {
					job.cleanup();
				} catch (Exception e) {
					fail("Should not throw exception here");
				}				
			};
		}.start();
		
		job.cleanup();

		//Mockito assumes mocked object methods are only called once
		verify(logEntryDao).removeBefore((DateTime)any());
	}
	
	/**
	 * Test that job is not running after exception is throw. This allows us to run the job again.
	 */
	@Test
	public void assumeNotRunningOnException() throws Exception{
		Exception ex = new RuntimeException();
		doThrow(ex).when(logEntryDao).removeBefore((DateTime)any());
		try{
			job.cleanup();
		} catch(RuntimeException e) {
			assertEquals(ex, e);
		}
		assertFalse(job.isRunning());
	}
}