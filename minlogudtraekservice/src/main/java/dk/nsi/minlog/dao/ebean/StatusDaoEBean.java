package dk.nsi.minlog.dao.ebean;

import java.sql.Timestamp;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;

import dk.nsi.minlog.dao.StatusDao;

@Repository
public class StatusDaoEBean implements StatusDao{
	@Inject
	EbeanServer ebeanServer;
	
	
	@Override
	public DateTime getLastUpdated() {
		SqlRow row = ebeanServer.createSqlQuery("SELECT * FROM status LIMIT 1").findUnique();
		//If there does not exist a from date, we just pick the earliest possible date
		if(row != null){
	        Timestamp date = row.getTimestamp("lastUpdated");
	        return new DateTime(date.getTime());
		} else {
			return new DateTime(0);
		}		
	}

	@Override
	public void setLastUpdated(DateTime lastUpdated) {
		//Create an entry or update existing
		SqlUpdate update = ebeanServer.createSqlUpdate("INSERT INTO status (id, lastUpdated) VALUES(1, :lastUpdated) ON DUPLICATE KEY UPDATE lastUpdated = :lastUpdated");
		update.setParameter("lastUpdated", new Timestamp(lastUpdated.getMillis()));
		
		update.execute();
		
	}
}
