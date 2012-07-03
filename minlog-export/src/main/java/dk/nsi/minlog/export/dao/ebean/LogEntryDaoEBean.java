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
package dk.nsi.minlog.export.dao.ebean;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.avaje.ebean.SqlUpdate;

import dk.nsi.minlog.dao.ebean.SupportDao;
import dk.nsi.minlog.domain.LogEntry;
import dk.nsi.minlog.export.dao.LogEntryDao;
import dk.nsi.minlog.export.dao.StatusDao;

@Repository
public class LogEntryDaoEBean extends SupportDao<LogEntry> implements LogEntryDao {
	private static Logger logger = Logger.getLogger(LogEntryDaoEBean.class);

	@Inject StatusDao statusDao;
	
	protected LogEntryDaoEBean() {
		super(LogEntry.class);
	}
	
	@Override
	@Transactional
	public long removeBefore(DateTime date){
		SqlUpdate query = ebeanServer.createSqlUpdate("DELETE FROM LogEntry WHERE tidspunkt <= :date");
		int numberOfIds = query.execute();
		return numberOfIds;
	}

	@Override
	@Transactional
	public void save(List<LogEntry> logEntries) {
		// Assume logEntries are sorted, so latestUpdate will be the last entry
		LogEntry last = logEntries.get(logEntries.size() - 1);

		if (logger.isDebugEnabled()) {
			logger.debug("Updating lastUpdated to " + last.getLastUpdated());
		}

		ebeanServer.save(logEntries);
		statusDao.setLastUpdated(last.getLastUpdated());
	}
}