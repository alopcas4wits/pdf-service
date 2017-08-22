package de.wits.pdf.task;

import de.wits.pdf.configuration.FileSystemPathProperties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by alberto on 22.08.17.
 */
@Component
public class CleanupTask {

    @Autowired
    FileSystemPathProperties fileSystemPathProperties;

    private static final Logger LOG = LoggerFactory.getLogger(CleanupTask.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 5000)//cron = "0 0 4 1/2 * ? *")
    public void clearTempFolder() {
        LOG.info("Clearing temporal folder");
        try {
            FileUtils.cleanDirectory(new File(fileSystemPathProperties.getTemporal()));
        } catch (IOException e) {
            LOG.error("Could not clear the temp folder ", e);
        }
    }
}
