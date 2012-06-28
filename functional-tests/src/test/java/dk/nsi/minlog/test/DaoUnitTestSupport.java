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
package dk.nsi.minlog.test;

import static org.mockito.Mockito.*;

import java.io.File;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.googlecode.flyway.core.Flyway;
import com.mysql.management.driverlaunched.ServerLauncherSocketFactory;

import dk.nsi.minlog.config.ApplicationRootConfig;

/**
 * Setups the database via TestDBConfig between tests
 *
 * Notice this will give us ebean warnings as ebean does not like 
 * swapping of ebean instances. This does not give any problems.
 * 
 * @author kpi
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDBConfig.class, DaoUnitTestSupport.MockContext.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class DaoUnitTestSupport extends AbstractJUnit4SpringContextTests {	
	@Inject 
	Flyway flyway;
	
	@Inject
	File databaseDir;

	public static class MockContext {
        @Bean
        public ServletContext servletContext() {
            return mock(ServletContext.class);
        }
    }
    
    @Before
    public void setup() throws Exception {
    	flyway.clean();
        flyway.setBaseDir("db/migration");
        flyway.setTable("schema_version");
    	flyway.migrate();
        
        flyway.setBaseDir("db/testData");
        flyway.setTable("testschema_version");
    	flyway.migrate();
    }    
    
    @After
    public void tearDown() throws Exception {
        ServerLauncherSocketFactory.shutdown(databaseDir, null);
    }
}