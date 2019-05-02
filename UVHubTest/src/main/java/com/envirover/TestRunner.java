package com.envirover;

import com.envirover.uvhub.UVHubTest;
import com.envirover.uvtracks.UVTracksTest;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

class TestRunner {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(UVHubTest.class, UVTracksTest.class);
          
        System.out.println("Test failures:");
        for (Failure failure : result.getFailures()) {
            System.out.print(failure.toString());
        }

        System.exit(result.getFailureCount());
    }
}