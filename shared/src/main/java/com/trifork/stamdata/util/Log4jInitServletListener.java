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
package com.trifork.stamdata.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

/**
 * 
 * 
 * Idea borrowed from http://logging.apache.org/log4j/1.2/manual.html - modified to be a ServletContextListener instead.
 */
public class Log4jInitServletListener implements ServletContextListener {

    private static final String LOG4J_CONFIG_FILE_PARAM = "log4j-config-file";
    private static final String JBOSS_SERVER_CONFIG_URL_PROPERTY = "jboss.server.config.url";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String configDir = System.getProperty(JBOSS_SERVER_CONFIG_URL_PROPERTY);
        String file = sce.getServletContext().getInitParameter(LOG4J_CONFIG_FILE_PARAM);

        // if the log4j-config-file is not set, then no point in trying
        if (file != null) {
            String fullConfigFilePath = "";
            if (file.startsWith("/")) {
                //Absolute path
                fullConfigFilePath = file;
            } else {
                if (configDir != null) {
                    //Relative path - use value of property jboss.server.config.url as prefix
                    configDir = configDir.substring(configDir.indexOf(":")+1);
                    fullConfigFilePath = configDir + file;
                } else {
                    System.err.println("ERROR: System property '" + JBOSS_SERVER_CONFIG_URL_PROPERTY + "' must be defined in order for log4j to be configured using a relative path for servlet param '"+LOG4J_CONFIG_FILE_PARAM+"'");
                    return;
                }
            }

            try {
                Log4jRepositorySelector.init(sce.getServletContext(), fullConfigFilePath);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }


        } else {
            System.err.println("ERROR: Servlet init parameter '" + LOG4J_CONFIG_FILE_PARAM + "' must be defined in web.xml in order for log4j to be configured for this web app");
        }


    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
