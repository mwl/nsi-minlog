package dk.nsi.minlog.web;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;

import dk.nsi.minlog.server.dao.LogEntrySearchDao;

@Repository
public class MinLogImportJob {
	@Inject
	private LogEntrySearchDao logEntrySearchDao;
			
	public void startImport(){
		//logEntrySearchDao.
	}
}