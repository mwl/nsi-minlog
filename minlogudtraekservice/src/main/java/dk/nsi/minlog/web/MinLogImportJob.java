package dk.nsi.minlog.web;

import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import com.avaje.ebean.annotation.Transactional;

import dk.nsi.minlog.domain.LogEntry;
import dk.nsi.minlog.server.dao.LogEntryDao;
import dk.nsi.minlog.server.dao.LogEntrySearchDao;
import dk.nsi.minlog.server.dao.StatusDao;


/**
 * Imports log entries into a database.
 *  
 * @author kpi
 *
 */

@Repository
public class MinLogImportJob {
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
	@Scheduled(cron="${minlog.cleanup.cron}")
	public void startImport(){
		DateTime from = getLastUpdated();
		DateTime to = DateTime.now().minusMillis(delay);
		
		List<LogEntry> logEntries = logEntrySearchDao.findLogEntries(from, to);
		updateDatabase(logEntries);
	}
	
	@Transactional
	private DateTime getLastUpdated(){
		return statusDao.getLastUpdated();
	}
	
	/**
	 * We want to make the update in a seperate transaction, 
	 * so we do not lock down the datasource while searching
	 * 
	 * @param logEntries
	 */	
	@Transactional
	private void updateDatabase(List<LogEntry> logEntries){
		logEntryDao.save(logEntries);
	}
}