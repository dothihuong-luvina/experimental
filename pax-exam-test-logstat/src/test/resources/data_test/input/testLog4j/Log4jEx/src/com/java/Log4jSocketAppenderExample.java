package com.java;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log4jSocketAppenderExample {
	static Logger logger = Logger.getLogger(Log4jSocketAppenderExample.class);
	public static void main(String[] args) throws InterruptedException {
		int i = 0;
		while (true) {
			// PropertiesConfigurator is used to configure logger from
			// properties file
			String wd = System.getProperty("user.dir");
			PropertyConfigurator.configure(wd + "/src/com/java/log4j.properties");

			// These logs will be sent to socket server as configured in
			logger.error("Log4j error message!!");
			//logger.debug("Log4j debug message!!");
			Log4jSocketAppenderExample obj = new Log4jSocketAppenderExample();
			obj.testFunction();
			System.out.println(i++);
			Thread.sleep(3000);
		}
	}

	public void testFunction() {
		logger.info("TEST FUNCTION LOGS !");
		
	}
}