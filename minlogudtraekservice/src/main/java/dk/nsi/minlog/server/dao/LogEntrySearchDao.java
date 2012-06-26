package dk.nsi.minlog.server.dao;

import java.util.List;

import dk.nsi.minlog.domain.LogEntry;

public interface LogEntrySearchDao {
	public List<LogEntry> findLogEntries();
}
