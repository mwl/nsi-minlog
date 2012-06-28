package com.trifork.stamdata.util;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
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
