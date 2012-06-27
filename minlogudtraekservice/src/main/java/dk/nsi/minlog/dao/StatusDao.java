package dk.nsi.minlog.dao;

import org.joda.time.DateTime;

public interface StatusDao {
	public DateTime getLastUpdated();	
	public void setLastUpdated(DateTime lastUpdated);	
}