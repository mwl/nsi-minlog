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
package dk.nsi.minlog.server.dao.ebean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Set;

import javax.inject.Inject;

import org.junit.Test;

import dk.nsi.minlog.test.IntegrationUnitTestSupport;
import dk.nsi.minlog.ws.dao.ebean.WhitelistCheckerDefault;


public class WhitelistCheckerDefaultTest extends IntegrationUnitTestSupport {
    @Inject
    WhitelistCheckerDefault whitelistChecker;

    /**
     * Check if we can fetch legal cvr numbers from a database
     * 
     * @throws Exception
     */
    
    @Test
    public void canFetchTestDataCvrNumbers() throws Exception {
        Set<String> testWhitelist = whitelistChecker.getLegalCvrNumbers("test");
        assertEquals(2, testWhitelist.size());
        assertThat(testWhitelist, hasItems("1", "2"));
    }
}