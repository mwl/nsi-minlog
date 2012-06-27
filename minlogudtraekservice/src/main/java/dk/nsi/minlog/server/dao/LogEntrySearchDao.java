package dk.nsi.minlog.server.dao;

import java.util.List;

import org.joda.time.DateTime;

import dk.nsi.minlog.domain.LogEntry;

public interface LogEntrySearchDao {
	public List<LogEntry> findLogEntries(DateTime from, DateTime to);
}
