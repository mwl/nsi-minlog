package dk.nsi.minlog.server.dao.splunk;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.splunk.Job;
import com.splunk.ResultsReader;
import com.splunk.ResultsReaderXml;
import com.splunk.Service;

import dk.nsi.minlog.domain.LogEntry;
import dk.nsi.minlog.server.dao.LogEntrySearchDao;
import java.util.UUID;
@Repository
public class LogEntrySearchDaoSplunk implements LogEntrySearchDao {
	static final Logger logger = Logger.getLogger(LogEntrySearchDaoSplunk.class);

	@Value("${minlog.splunk.sleep}") 
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
	public List<LogEntry> findLogEntries()  {
		Job job = splunkService.getJobs().create("search index=main sourcetype=minlog | fields " + FIELDS + " | sort by _indextime asc");
		
		//Wait for splunk query to complete
		logger.debug("Waiting for splunk to complete job");
		while (!job.isDone()) {
			logger.trace("Busy spinning...");
			try {
				Thread.sleep(sleep);
			} catch (Exception e) {}
			
			job.refresh();
		}
		logger.debug("Done waiting for splunk");
		
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
	
	private LogEntry transformMapToEntity(final Map<String, String> map) throws IllegalArgumentException{
		if(logger.isTraceEnabled()){
			logger.trace("Mapping to entity: \n" + map);			
		}
		return new LogEntry() {{
			setRegKode(UUID.randomUUID().toString());
			setTidspunkt(DateTime.parse(map.get("EventDateTime")));
			setCprNrBorger(map.get("PersonCivilRegistrationIdentifier"));
			setBruger(map.get("UserIdentifier"));
			setAnsvarlig(map.get("UserIdentifierOnBehalfOf"));
			setOrgUsingID(map.get("HealthcareProfessionalOrganization"));
			setSystemName(map.get("SourceSystemIdentifier"));
			setHandling(map.get("Activity"));
			setSessionId(map.get("SessionId"));			
		}};
	}	
}