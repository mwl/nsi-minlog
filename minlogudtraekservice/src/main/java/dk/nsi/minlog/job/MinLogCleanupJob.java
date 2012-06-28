package dk.nsi.minlog.job;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dk.nsi.minlog.dao.LogEntryDao;
/**
 * A job which cleans the database by deleting entries that are older then 2 years.
 */	
@Repository
public class MinLogCleanupJob {	
	private static Logger logger = Logger.getLogger(MinLogCleanupJob.class);

	@Inject
	private LogEntryDao logEntryDao;
	
	private boolean running;
		
	@Transactional
	@Scheduled(cron = "${minlog.cleanup.cron}")
	public void cleanup(){
		// Only one job is allow to run at a time.
		if(!running){
			running = true;
			try{
				DateTime date = DateTime.now().minusYears(2);	
				logger.info("Running cleanup job for entries before " + date);
				long entries = logEntryDao.removeBefore(date);
				logger.info("Deleted " + entries + " entries");
			} catch(Exception e){
				logger.warn("Failed to execute cleanup job", e);
			}
			running = false;
		}
	}
	
	public boolean isRunning(){
		return running;
	}
}