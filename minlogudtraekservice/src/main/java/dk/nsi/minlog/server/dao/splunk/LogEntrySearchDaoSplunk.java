package dk.nsi.minlog.server.dao.splunk;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.splunk.Job;
import com.splunk.ResultsReader;
import com.splunk.ResultsReaderXml;
import com.splunk.Service;

import dk.nsi.minlog.domain.LogEntry;
import dk.nsi.minlog.server.dao.LogEntrySearchDao;

@Repository
public class LogEntrySearchDaoSplunk implements LogEntrySearchDao {
	private static final Logger logger = Logger.getLogger(LogEntrySearchDaoSplunk.class);
	
	private static final String FIELDS = "_indextime, _time, PersonCivilRegistrationIdentifier, UserIdentifier, UserIdentifierOnBehalfOf, HealthcareProfessionalOrganization, SourceSystemIdentifier, Activity, SessionId";
	
	private Map<String, String> resultOptions;  
	
	@Inject
	Service splunkService;
	
	public LogEntrySearchDaoSplunk(){
		resultOptions = new HashMap<String, String>();
		resultOptions.put("field_list", FIELDS);
	}
	
	
	@Override
	public List<LogEntry> findLogEntries()  {
		
		Job job = splunkService.getJobs().create("search index=main sourcetype=minlog | fields " + FIELDS + " | sort by _indextime asc");
		
		//Wait for splunk query to complete
		while (!job.isDone()) {
			logger.debug("Waiting for splunk to complete job");
			try {
				Thread.sleep(2000);
			} catch (Exception e) {}
			
			job.refresh();
		}		
		
		Map<String, String> resultMap;	
		InputStream stream = job.getResults(resultOptions);
		ResultsReader resultsReader;
		try {
			resultsReader = new ResultsReaderXml(stream);
			while ((resultMap = resultsReader.getNextEvent()) != null) {
				for (String key: resultMap.keySet()){
					System.out.println(key + " --> " + resultMap.get(key));
				}
			}
		//api for result reader is a bit ugly, it throws Exception
		} catch (Exception e) {
		}
		
		return new ArrayList<LogEntry>();
	}		
}