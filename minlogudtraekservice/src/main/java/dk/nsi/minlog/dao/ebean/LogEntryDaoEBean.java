package dk.nsi.minlog.dao.ebean;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import com.avaje.ebean.ExpressionList;

import dk.nsi.minlog.dao.LogEntryDao;
import dk.nsi.minlog.domain.LogEntry;

@Repository
public class LogEntryDaoEBean extends SupportDao<LogEntry> implements LogEntryDao {
	protected LogEntryDaoEBean() {
		super(LogEntry.class);
	}

	@Override
	public List<LogEntry> findByCPRAndDates(String cpr, DateTime from, DateTime to) {
		ExpressionList<LogEntry> query = query().where().eq("cprNrBorger", cpr);
		if(from != null){
			query = query.ge("tidspunkt", from);
		}
		
		if(to != null){
			query.le("tidspunkt", to);
		}		
		return query.findList();
	}
	
	@Override
	public long removeBefore(DateTime date){
		ExpressionList<LogEntry> query = query().where().le("tidspunkt", date);
		List<Object> ids = query.findIds();
		long numberOfIds = ids.size();
		ebeanServer.delete(LogEntry.class, ids);
		
		return numberOfIds;
	}

	@Override
	public void save(List<LogEntry> logEntries) {
		ebeanServer.save(logEntries);
	}
}