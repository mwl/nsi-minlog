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
package dk.nsi.minlog.job;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import com.avaje.ebean.annotation.Transactional;

import dk.nsi.minlog.dao.LogEntryDao;
import dk.nsi.minlog.dao.LogEntrySearchDao;
import dk.nsi.minlog.dao.StatusDao;
import dk.nsi.minlog.domain.LogEntry;


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
				DateTime from = getLastUpdated();
				DateTime to = DateTime.now().minusMillis(delay);

				if (logger.isDebugEnabled()) {
					logger.debug("Fetching data from " + from + " to " + to);
				}

				List<LogEntry> logEntries = logEntrySearchDao.findLogEntries(from, to);

				if (logEntries.size() > 0) {
					// Assume logEntries are sorted, so latestUpdate will be the
					// last
					// entry
					LogEntry last = logEntries.get(logEntries.size() - 1);

					if (logger.isDebugEnabled()) {
						logger.debug("Updating lastUpdated to " + last.getLastUpdated());
					}

					updateDatabase(logEntries, last.getLastUpdated());
				}
			} catch (Exception e) {
				logger.error("Import failed, setting running to false for next iteration");
			}

			running = false;
		}
	}
	
	@Transactional
	private DateTime getLastUpdated(){
		return statusDao.getLastUpdated();
	}
	
	/**
	 * We want to make the update in a seperate transaction, 
	 * so we do not lock down the datasource while searching.
	 * 
	 * We also make sure we update the status in the same transaction as we insert the LogEntries.
	 * 
	 * @param logEntries
	 */	
	@Transactional
	private void updateDatabase(List<LogEntry> logEntries, DateTime to){
		logEntryDao.save(logEntries);
		statusDao.setLastUpdated(to);
	}	
}