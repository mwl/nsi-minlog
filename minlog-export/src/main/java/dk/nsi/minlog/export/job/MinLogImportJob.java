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

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import dk.nsi.minlog.domain.LogEntry;
import dk.nsi.minlog.export.dao.LogEntryDao;
import dk.nsi.minlog.export.dao.LogEntrySearchDao;
import dk.nsi.minlog.export.dao.StatusDao;
import dk.sdsd.nsp.slalog.api.SLALogItem;
import dk.sdsd.nsp.slalog.api.SLALogger;

/**
 * Imports log entries from splunk into a database.
 *  
 * @author kpi
 *
 */

@Repository
public class MinLogImportJob {
	private static Logger logger = Logger.getLogger(MinLogImportJob.class);

	@Inject
	private LogEntrySearchDao logEntrySearchDao;
	
	@Inject
	private LogEntryDao logEntryDao;
	
	@Inject 
	private StatusDao statusDao; 
	
	@Inject
	private SLALogger slaLogger;
	
	@Value("${minlog.import.delay}")
	public Integer delay;
	
	private boolean running;
	
	/**
	 * Fetches data from Splunk and inserts saves it via LogEntryDao
	 */	
	@Scheduled(cron="${minlog.import.cron}")
	public void startImport(){
		
		if (!running) {
			running = true;

			try {
				DateTime from = statusDao.getLastUpdated();
				DateTime to = DateTime.now().minusMillis(delay);

				if (logger.isDebugEnabled()) {
					logger.debug("Fetching data from " + from + " to " + to);
				}

				SLALogItem slaLogImport = slaLogger.createLogItem("Splunk import", "Fetching data from " + from + " to " + to);
				List<LogEntry> logEntries = logEntrySearchDao.findLogEntries(from, to);
				slaLogImport.setCallResultOk();
				slaLogImport.store();
				

				if (logEntries.size() > 0) {
					SLALogItem slaLogSave = slaLogger.createLogItem("Database save", "Saving " + logEntries.size() + " items");
					//save also updates the status in same transaction
					logEntryDao.save(logEntries);
					slaLogSave.setCallResultOk();
					slaLogImport.store();
					
				}
			} catch (Exception e) {
				logger.error("Import failed, setting running to false for next iteration", e);
			}

			running = false;
		}
	}
			
	/**
	 * Check if import is running
	 * 
	 * @return true if is running.
	 */
	public boolean isRunning() {
		return running;
	}
}