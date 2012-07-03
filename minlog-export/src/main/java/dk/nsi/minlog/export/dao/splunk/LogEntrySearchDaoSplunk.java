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
package dk.nsi.minlog.export.dao.splunk;

import java.io.IOException;
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

import dk.nsi.minlog.domain.LogEntry;
import dk.nsi.minlog.export.dao.LogEntrySearchDao;
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
		String query = "search index=main sourcetype=minlog  (_indextime > "+ (from.getMillis()/1000) + " AND _indextime < " + (to.getMillis()/1000) + ") | fields " + FIELDS + " | sort by _indextime asc";
		
		logger.debug("Splunk query:" + query);
		
		Job job = splunkService.getJobs().create(query);
		
		//Wait for splunk query to complete
		logger.debug("Waiting for splunk to complete job");
		while (!job.isDone()) {
			logger.trace("Busy spinning...");
			try {
				Thread.sleep(sleep);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
			}
			
			job.refresh();
			
			if(logger.isDebugEnabled()){
				logger.debug("Checking job: " + (job.getDoneProgress() * 100.0f) + "% done");
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("Done waiting for splunk, result size:" + job.getResultCount());
		}
		
		
		List<LogEntry> result = new ArrayList<LogEntry>();
		 
		InputStream stream = job.getResults(resultOptions);

		try {
			ResultsReader resultsReader = new ResultsReaderXml(stream);
			try{
				Map<String, String> resultMap;
				while ((resultMap = resultsReader.getNextEvent()) != null) {
					result.add(transformMapToEntity(resultMap));
				}
			} finally {
				resultsReader.close();
			}
		//api for ResultsReaderXml is a bit ugly, it throws Exception
		} catch (Exception e) {
			logger.error("Failed to transform xml to entity", e);
			throw new RuntimeException("Failed to transform xml to entity from splunk");
		} finally {
			try {
				stream.close();				
			} catch(IOException e){
				logger.error("Failed to close stream", e);
			}
		}
		
		return result;
	}
	
	private static LogEntry transformMapToEntity(final Map<String, String> map) {
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
		
		//Notice splunk return in seconds since epoch, datetime uses millis.
		long indexTime = Long.parseLong(map.get("_indextime")) * 1000;
		logEntry.setLastUpdated(new DateTime(indexTime));		
		return logEntry;
	}		
}