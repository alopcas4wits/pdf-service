package de.wits.pdf.service;

import de.wits.pdf.configuration.FileSystemPathProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

@Service
public class PdfServiceImpl implements PdfService {

    private static final Logger LOG = LoggerFactory.getLogger(PdfServiceImpl.class);
    private static final int RUN_COUNT = 3;

    static final Pattern MEDIA_REGEX = Pattern.compile("img:(https?:\\/\\/[^}]*)");

    private transient FileSystemPathProperties fileSystemPathConfig;

    @Autowired
    public PdfServiceImpl(FileSystemPathProperties fileSystemPathConfig) {
        this.fileSystemPathConfig = fileSystemPathConfig;
    }

    @Override
    public File getPdf(String template) throws PDFCreationFailedException {
        File file = null;
        try {
            File tmpFile = new File(fileSystemPathConfig.getTemporal() + File.separator + UUID.randomUUID(), "template.tex");
            template = processTemplateMedia(template, tmpFile.getParentFile());
            FileUtils.writeStringToFile(tmpFile, template, "UTF-8");
            file = generate(tmpFile);
        } catch (Exception e) {
            throw new PDFCreationFailedException("Could not create PDF Document.", e);
        }

        return file;
    }

    public File generate(File templateFile) throws PDFCreationFailedException, IOException {
        return this.generate(templateFile, true);
    }

    public File generate(File templateFile, boolean optimize) throws PDFCreationFailedException, IOException {
        File outputFile = null;
        try {
            String pdfLatexPath = fileSystemPathConfig.getPdfLatex();
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

            for (int i = 1; i <= RUN_COUNT; ++i) {
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
            String resultFileName = templateFile.getName().replaceAll(".tex$", ".pdf");
            if (optimize) {
                String[] optimizeArgs = new String[]{
                    "gs", "-sDEVICE=pdfwrite", "-dCompatibilityLevel=1.4", "-dNOPAUSE", "-dQUIET", "-dBATCH", "-sOutputFile=final-" + resultFileName, resultFileName
                };
                LOG.info("Running ghostscript optimization using {}", optimizeArgs);
                ProcessBuilder optimizeProcessBuilder = new ProcessBuilder(optimizeArgs);
                optimizeProcessBuilder.redirectErrorStream(true);
                optimizeProcessBuilder.directory(templateFile.getParentFile());
                Process process = optimizeProcessBuilder.start();
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
                    if (process.waitFor() > 0) {
                        LOG.warn("Optimize failed. Using non optimized");
                        LOG.warn("Output: {}", outputStream);
                    } else {
                        resultFileName = "final-" + resultFileName;
                    }
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted while optimizing. use normal", e);
                }
            }
            outputFile = new File(new FileSystemResource(new File(templateFile.getParent())).getFile(), resultFileName);
            LOG.info("PDF successfully created. Moving to output resource: {}", outputFile.getAbsolutePath());
        } finally {
            // Clear the temp folder after the work is done
            //FileUtils.deleteDirectory(templateFile.getParentFile());
        }

        return outputFile;
    }

    String processTemplateMedia(String template, File tempFolder) {
        Matcher matcher = MEDIA_REGEX.matcher(template);
        String patchedTemplate = template;
        int mediaIndex = 0;
        while (matcher.find()) {
            String mediaURL = matcher.group(1);
            mediaURL = mediaURL.replaceAll("}", ""); //FIXME: write a decent regex
            String filteredURL = mediaURL.replace("\\", "");
            filteredURL = filteredURL.replace("&amp;", "&");
            filteredURL = filteredURL.replace("\\&", "&");
            String mediaName = "img" + mediaIndex + ".png";
            File mediaFolder = new File(tempFolder, mediaName);
            LOG.info("Downloading media at " + filteredURL);
            try {
                FileUtils.copyURLToFile(new URL(filteredURL), mediaFolder);
            } catch (IOException ex) {
                LOG.warn("Ignoring malformed URL: " + mediaURL);
            }

            patchedTemplate = patchedTemplate.replace("img:" + mediaURL, mediaName);
            mediaIndex++;
        }

        return patchedTemplate;
    }

}
