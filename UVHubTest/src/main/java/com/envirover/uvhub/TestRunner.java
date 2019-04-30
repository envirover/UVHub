package com.envirover.uvhub;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

class TestRunner {
    public static void main(String[] args) {
        // Configure LOG4J
        ConsoleAppender console = ConsoleAppender.createDefaultAppenderForLayout(PatternLayout.createDefaultLayout()); 
 
        org.apache.logging.log4j.core.Logger coreLogger
        = (org.apache.logging.log4j.core.Logger)LogManager.getRootLogger();
   
        coreLogger.addAppender(console);

        Result result = JUnitCore.runClasses(UVHubTest.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

        System.exit(result.getFailures().size());
    }
}