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

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import dk.nsi.minlog.export.dao.LogEntryDao;
import dk.sdsd.nsp.slalog.api.SLALogItem;
import dk.sdsd.nsp.slalog.api.SLALogger;
/**
 * A job which cleans the database by deleting entries that are older then 2 years.
 */	
//@Repository
public class MinLogCleanupJob {	
	private static Logger logger = Logger.getLogger(MinLogCleanupJob.class);

	@Inject
	private LogEntryDao logEntryDao;
	
	@Inject
	private SLALogger slaLogger;
	
	private boolean running;
		
	@Transactional
	@Scheduled(cron = "${minlog.cleanup.cron}")
	public void cleanup() throws Exception{
		// Only one job is allow to run at a time.
		if(!running){
			running = true;
			try{
				DateTime date = DateTime.now().minusYears(2);	
				SLALogItem slaLogCleanup = slaLogger.createLogItem("Database cleanup", "clean up entries before " + date);
				logger.info("Running cleanup job for entries before " + date);
				
				long entries = logEntryDao.removeBefore(date);
				
				slaLogCleanup.setCallResultOk();
				slaLogCleanup.store();
				
				logger.info("Deleted " + entries + " entries");
			} catch(Exception e){
				logger.warn("Failed to execute cleanup job", e);
				throw e;
			} finally {
				running = false;				
			}
		}
	}
	
	public boolean isRunning(){
		return running;
	}
}