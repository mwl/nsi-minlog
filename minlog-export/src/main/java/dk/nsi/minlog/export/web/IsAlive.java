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
package dk.nsi.minlog.export.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;
import javax.servlet.jsp.JspWriter;
import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.splunk.Service;


/**
 * Service to check if everything is okey.
 *
 * @author kpi
 *
 */
@Repository("isAlive")
public class IsAlive {

	@Inject
	DataSource dataSource;

	@Inject
	Service splunkService;

	/**
	 * Checks if we have access to the database by doing a simple query.
	 *
	 * @param out Writes out the result to this jsp writer.
	 * @throws IOException
	 * @throws SQLException
	 */
	public void checkAll(JspWriter out) throws Exception{
		Statement statement = null;
		Connection connection = null;

		try {
			// Checking database connection
			connection = dataSource.getConnection();
			statement = connection.createStatement();

			ResultSet rs = statement.executeQuery("SELECT 1");
			if (!rs.next() || rs.getInt(1) != 1){
				throw new RuntimeException("Invalid result from database");
			}

			// Checking splunk connection (throws exception on error)
			splunkService.getInfo();
		}
		finally {
			try {
	                        if (statement != null) statement.close();
			}
			finally {
	                        if (connection != null) connection.close();
			}
		}
	}
}
