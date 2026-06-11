package org.levelci.selenium;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
class IOService {

    @SuppressWarnings("ALL")
    static synchronized boolean prepareFilesystem(String destination) {
        if (destination == null || destination.isBlank()) {
            return false;
        }
        var dest = new File(destination);
        if (!dest.exists() || !dest.isDirectory()) {
            log.debug("Creating directory {}", destination);
            if (!dest.mkdir()) {
                log.error("Could not create directory {}", destination);
                return false;
            }
        }
        return true;
    }
}
