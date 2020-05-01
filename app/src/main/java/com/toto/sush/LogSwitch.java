package com.toto.sush;

/**
 * Created by abhinavganguly on 28/05/2017.
 */

public class LogSwitch {

    private static int LOG_LEVEL = 4;

    public static boolean LOG_INFO = LOG_LEVEL>3;
    public static boolean LOG_WARN = LOG_LEVEL>2;
    public static boolean LOG_ERROR = LOG_LEVEL>0;


}
