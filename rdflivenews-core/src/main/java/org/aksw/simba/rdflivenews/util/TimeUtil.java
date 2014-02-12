package org.aksw.simba.rdflivenews.util;

import java.util.concurrent.TimeUnit;


public class TimeUtil {

    public static String convertMilliSeconds(long millis) {

        return String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
