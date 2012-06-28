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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.Map;

import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import com.splunk.Job;
import com.splunk.Service;

import dk.nsi.minlog.config.WSConfig;

/**
 * Webservice part of the setup.
 * 
 * @author kpi
 *
 */

@ContextConfiguration(classes = {WSConfig.class})
public abstract class IntegrationUnitTestSupport extends DaoUnitTestSupport {
	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	Service service;
	
	@Bean
	@SuppressWarnings("rawtypes")
	public Service splunkService() throws Exception{
		Job job = service.getJobs().create((String)any());
		InputStream stream = ClassLoader.class.getResourceAsStream("/splunk/queryResult.xml");
		when(job.getResults((Map)any())).thenReturn(stream);
		when(job.isDone()).thenReturn(false, true);
		
		return service;
	}
}