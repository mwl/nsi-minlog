package dk.nsi.minlog.server.dao.ebean;

import java.sql.Timestamp;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;

import dk.nsi.minlog.server.dao.StatusDao;

@Repository
public class StatusDaoEBean implements StatusDao{
	@Inject
	EbeanServer ebeanServer;
	
	
	@Override
	public DateTime getLastUpdated() {
		SqlRow row = ebeanServer.createSqlQuery("SELECT lastUpdated FROM status LIMIT 1").findUnique();
        Timestamp date = row.getTimestamp("lastUpdated");
        return new DateTime(date.getTime());
	}

	@Override
	public void setLastUpdated(DateTime lastUpdated) {
		SqlUpdate update = ebeanServer.createSqlUpdate("UPDATE lastUpdated SET lastUpdated = :lastUpdated");
		update.setParameter("lastUpdated", new Timestamp(lastUpdated.getMillis()));
		
		update.execute();
		
	}
}
