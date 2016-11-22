package de.wits.pdf.service;

import de.wits.pdf.configuration.FileSystemPathProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Service;

@Service
public class PdfServiceImpl implements PdfService {

  private static final Logger LOG = LoggerFactory.getLogger(PdfServiceImpl.class);
  private static final int RUN_COUNT = 3;

  @Autowired
  private transient FileSystemPathProperties fileSystemPathConfig;

  Resource outputResource;

  @PostConstruct
  void init() {
    outputResource = new FileSystemResource(new File(fileSystemPathConfig.getPdf())); // TODO call every time is needed
  }

  @Override
  public File getPdf(String template) throws PDFCreationFailedException {
    File file = null;
    try {
      File tmpFile = new File(fileSystemPathConfig.getTemporal() + UUID.randomUUID(), "template.tex");
      FileUtils.writeStringToFile(tmpFile, template);
      file = generate(tmpFile);
    } catch (Exception e) {
      throw new PDFCreationFailedException("Could not create PDF Document.", e);
    }

    return file;
  }

  public File generate(File templateFile) throws PDFCreationFailedException, IOException {
    File outputFile = null;
    try {
      String pdfLatexPath = "/usr/bin/latex";
      String pdfLatexMode = "--interaction=nonstopmode";
      String pdfLatexOutput = "--output-directory=" + templateFile.getParent();
      String shellEscapeCommand = "--shell-escape";
      String templatePath = templateFile.getAbsolutePath();
      String[] processArgs = new String[]{
        pdfLatexPath, templatePath, pdfLatexMode, pdfLatexOutput, shellEscapeCommand
      };
      ProcessBuilder processBuilder = new ProcessBuilder(processArgs);
      processBuilder.redirectErrorStream(true);
      processBuilder.directory(templateFile.getParentFile());

      for (int i = 0; i < RUN_COUNT; ++i) {
        Process process = processBuilder.start();
        LOG.debug("Doing PDF creating run {} with the arguments: {}", i, processArgs);
        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder outputBuilder = new StringBuilder();
        String line = null;

        try {
          while ((line = bufferedReader.readLine()) != null) {
            outputBuilder.append(line + System.getProperty("line.separator"));
          }
        } finally {
          bufferedReader.close();
        }

        String outputStream = outputBuilder.toString();
        try {
          int exitCode = process.waitFor();

          if (exitCode != 0) {
            LOG.warn("PDF Creation return non zero exit value: {}", exitCode);
            LOG.warn("Output: {}", outputStream);
            throw new PDFCreationFailedException("Template wrong. Non Zero exit code: " + exitCode);

          }
        } catch (InterruptedException ex) {
          LOG.warn("The process pdfLaTeX was interrupted and an exception occurred!", ex);
          LOG.warn("Output: {}", outputStream);
          throw new PDFCreationFailedException("pdfLaTeX was interrupted", ex);
        }
      }
      outputFile = new File(outputResource.getFile(), System.currentTimeMillis() + templateFile.getName().replaceAll(".tex$", ".pdf"));
      LOG.info("PDF successfully created. Moving to output resource: {}", outputFile.getAbsolutePath());
      WritableResource writableResource = new PathResource(outputFile.getAbsolutePath());

      try (OutputStream outputStream = writableResource.getOutputStream()) {
        outputStream.write(FileUtils.readFileToByteArray(new File(templateFile.getAbsolutePath().replaceAll(".tex$", ".dvi"))));
      }
    } finally {
      // Clear the temp folder after the work is done
      FileUtils.deleteDirectory(templateFile.getParentFile());
    }

    return outputFile;
  }

}
