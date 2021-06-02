package com.envirover;

import com.envirover.uvhub.UVHubTest;
import com.envirover.uvtracks.UVTracksTest;
import com.envirover.uvtracks.UVTracksV3Test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

class TestRunner {
    public static void main(String[] args) {
        System.out.println("UV Hub and UV Tracks integration test.");

        Result result = JUnitCore.runClasses(UVHubTest.class, UVTracksTest.class, UVTracksV3Test.class);

        if (result.wasSuccessful()) {
            System.out.println(
                    String.format("Test completed successfully in %.3f seconds.", result.getRunTime() / 1000.0));
        } else {
            System.out.println("Failed tests:");
            for (Failure failure : result.getFailures()) {
                System.out.println(failure.toString());
            }
        }

        System.exit(result.getFailureCount());
    }
}
