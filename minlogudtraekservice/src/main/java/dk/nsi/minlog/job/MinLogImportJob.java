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
 * Imports log entries into a database.
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
	
	/**
	 * Fetches data from Splunk and inserts saves it via LogEntryDao
	 */	
	@Scheduled(cron="${minlog.import.cron}")
	public void startImporc(){		
		DateTime from = getLastUpdated();			
		DateTime to = DateTime.now().minusMillis(delay);
		
		if(logger.isDebugEnabled()){
			logger.debug("Fetching data from " + from + " to " + to);			
		}
		
		List<LogEntry> logEntries = logEntrySearchDao.findLogEntries(from, to);
		
		//Assume logEntries are sorted, so latestUpdate will be the last entry
		LogEntry last = logEntries.get(logEntries.size()-1);

		if(logger.isDebugEnabled()){
			logger.debug("Updating lastUpdated to " + last.getLastUpdated());
		}
		
		updateDatabase(logEntries, 	last.getLastUpdated());
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