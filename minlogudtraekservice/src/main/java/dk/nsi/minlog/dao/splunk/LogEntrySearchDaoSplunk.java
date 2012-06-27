package dk.nsi.minlog.dao.splunk;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.splunk.Job;
import com.splunk.ResultsReader;
import com.splunk.ResultsReaderXml;
import com.splunk.Service;

import dk.nsi.minlog.dao.LogEntrySearchDao;
import dk.nsi.minlog.domain.LogEntry;
@Repository
public class LogEntrySearchDaoSplunk implements LogEntrySearchDao {
	static final Logger logger = Logger.getLogger(LogEntrySearchDaoSplunk.class);

	@Value("${minlog.import.sleep}") 
    public Integer sleep;
		
	@Inject
	Service splunkService;

	
	Map<String, String> resultOptions;  
	static final String FIELDS = "_indextime, _time, EventDateTime, PersonCivilRegistrationIdentifier, UserIdentifier, UserIdentifierOnBehalfOf, HealthcareProfessionalOrganization, SourceSystemIdentifier, Activity, SessionId";
	
	public LogEntrySearchDaoSplunk(){
		resultOptions = new HashMap<String, String>();
		resultOptions.put("field_list", FIELDS);
	}
	
	
	@Override
	public List<LogEntry> findLogEntries(DateTime from, DateTime to)  {
		String query = "search index=main sourcetype=minlog  (_indextime > "+ from.getMillis() + " AND _indextime < " + to.getMillis() + ") | fields " + FIELDS + " | sort by _indextime asc";
		
		logger.debug("Splunk query:" + query);
		
		Job job = splunkService.getJobs().create(query);
		
		//Wait for splunk query to complete
		logger.debug("Waiting for splunk to complete job");
		while (!job.isDone()) {
			logger.trace("Busy spinning...");
			try {
				Thread.sleep(sleep);
			} catch (Exception e) {}
			
			job.refresh();
			
			if(logger.isDebugEnabled()){
				logger.debug("Checking job: " + (job.getDoneProgress() * 100.0f) + "% done");
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("Done waiting for splunk, result size:" + job.getResultCount());
		}
		
		
		List<LogEntry> result = new ArrayList<LogEntry>();
		 
		Map<String, String> resultMap;	
		InputStream stream = job.getResults(resultOptions);

		ResultsReader resultsReader;
		try {
			resultsReader = new ResultsReaderXml(stream);
			while ((resultMap = resultsReader.getNextEvent()) != null) {
				result.add(transformMapToEntity(resultMap));
			}
		//api for ResultsReaderXml is a bit ugly, it throws Exception
		} catch (Exception e) {
			logger.error("Failed to transform xml to entity", e);
			throw new RuntimeException("Failed to transform xml to entity from splunk");
		}
		
		return result;
	}
	
	private static LogEntry transformMapToEntity(final Map<String, String> map) throws IllegalArgumentException{
		if(logger.isTraceEnabled()){
			logger.trace("Mapping to entity: \n" + map);			
		}
		LogEntry logEntry = new LogEntry();
		
		logEntry.setRegKode(UUID.randomUUID().toString());
		logEntry.setTidspunkt(DateTime.parse(map.get("EventDateTime")));
		logEntry.setCprNrBorger(map.get("PersonCivilRegistrationIdentifier"));
		logEntry.setBruger(map.get("UserIdentifier"));
		logEntry.setAnsvarlig(map.get("UserIdentifierOnBehalfOf"));
		logEntry.setOrgUsingID(map.get("HealthcareProfessionalOrganization"));
		logEntry.setSystemName(map.get("SourceSystemIdentifier"));
		logEntry.setHandling(map.get("Activity"));
		logEntry.setSessionId(map.get("SessionId"));
		logEntry.setLastUpdated(DateTime.parse(map.get("_indextime")));		
		return logEntry;
	}		
}