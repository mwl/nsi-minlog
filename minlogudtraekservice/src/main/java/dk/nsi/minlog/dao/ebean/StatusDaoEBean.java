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
