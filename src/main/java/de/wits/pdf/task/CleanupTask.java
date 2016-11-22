package de.wits.pdf.task;

import de.wits.pdf.configuration.FileSystemPathProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author alberto
 */
@Component
public class CleanupTask {

  private static final Logger LOG = LoggerFactory.getLogger(CleanupTask.class);

  private File outputFolder;

  @Autowired
  FileSystemPathProperties fileSystemPathConfig;

  @PostConstruct
  void init() {
    outputFolder = new File(fileSystemPathConfig.getPdf());
  }

  @Scheduled(fixedRate = 5000)
  public void cleanOldPdfFiles() throws IOException {
    for (File file : outputFolder.listFiles()) {
      if (file.isFile()) {
        BasicFileAttributes attr = Files.readAttributes(Paths.get(file.getAbsolutePath()), BasicFileAttributes.class);
        if (System.currentTimeMillis() - attr.creationTime().toMillis() > 60000) { // TODO set to one day
          // Delete files that were generated 1 minute ago
          file.delete();
        }
      }
    }
  }
}
