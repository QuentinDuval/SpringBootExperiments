package com.example.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static Logger getLogger() {
        return logger;
    }
}
