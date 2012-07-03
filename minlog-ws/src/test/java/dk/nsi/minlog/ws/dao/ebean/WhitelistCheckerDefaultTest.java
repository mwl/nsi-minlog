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
package dk.nsi.minlog.ws.dao.ebean;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlRow;

@RunWith(MockitoJUnitRunner.class)
public class WhitelistCheckerDefaultTest {

	@InjectMocks
	WhitelistCheckerDefault whitelistCheckerDefault = new WhitelistCheckerDefault();
	
	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	EbeanServer ebeanServer;
	
	/**
	 * Check if we can get a cvr from database.
	 * 
	 * @throws Exception
	 */	
	@Test
	public void  getLegalCvrNumbers() throws Exception{
		SqlRow row = mock(SqlRow.class);
		when(row.getString("legal_cvr")).thenReturn("result");		
		when(ebeanServer.createSqlQuery((String)any()).findSet()).thenReturn(Collections.singleton(row));
				
		Set<String> results = whitelistCheckerDefault.getLegalCvrNumbers("test");
		assertTrue(results.contains("result"));		
	}
}
