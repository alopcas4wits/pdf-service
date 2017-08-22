package de.wits.pdf.service;

import com.google.common.io.Files;
import de.wits.pdf.configuration.FileSystemPathProperties;
import de.wits.pdf.configuration.GenerationProperties;
import de.wits.pdf.model.PdfRequest;
import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfServiceImpl implements PdfService {

    private static final Logger LOG = LoggerFactory.getLogger(PdfServiceImpl.class);
    private static final int RUN_COUNT = 3;
    private static final int MAX_PROCESSES = 6;

    static final Pattern MEDIA_REGEX = Pattern.compile("img:(https?:\\/\\/[^}]*)");

    private transient FileSystemPathProperties fileSystemPathConfig;
    private transient GenerationProperties generationOptionsProperties;

    Queue<PdfRequest> processQueue, waitQueue;

    @Autowired
    public PdfServiceImpl(FileSystemPathProperties fileSystemPathConfig, GenerationProperties generationOptionsProperties) {
        this.fileSystemPathConfig = fileSystemPathConfig;
        this.generationOptionsProperties = generationOptionsProperties;
        processQueue = new ArrayBlockingQueue<>(MAX_PROCESSES, true);
        waitQueue = new LinkedList<>();
    }

    @Async
    @Override
    public CompletableFuture<File> getPdf(PdfRequest request) throws PDFCreationFailedException, ExecutionException, InterruptedException {
        File file = null;
        File tmpFile = new File(setupTmpFolder(), "template.tex");
        request.setTmpFolder(tmpFile);

        if (processQueue.offer(request)) {
            LOG.trace("Had space inmediately. Queue size; " + processQueue.size());
            file = generatePdfSyncronously(request);
        } else {
            waitQueue.offer(request);
            LOG.trace("Need to wait. Queue size is; " + processQueue.size());
            file = generatePdfAsynchronously(request).get();
        }

        return CompletableFuture.completedFuture(file);
    }

    private CompletableFuture<File> generatePdfAsynchronously(PdfRequest request) throws InterruptedException, PDFCreationFailedException {
        LOG.trace("Gotta wait. Queue size: " + waitQueue.size());
        while (!waitQueue.peek().equals(request)) { // Wait until you are the first one in the queue
            Thread.sleep(500);
        }
        LOG.trace("First one in the queue. Gotta wait since the amount of processes running is " + processQueue.size());
        while (processQueue.size() >= MAX_PROCESSES) { // Wait until there is space in the execution queue
            Thread.sleep(500);
        }
        processQueue.add(request);
        waitQueue.poll();

        return CompletableFuture.completedFuture(generatePdfSyncronously(request));
    }

    private File generatePdfSyncronously(PdfRequest request) throws PDFCreationFailedException {
        File file = null;
        try {
            String template = processTemplateMedia(request.getLatexTemplate(), request.getTmpFolder().getParentFile());
            file = process(template, request.getTmpFolder());
        } catch (Exception e) {
            throw new PDFCreationFailedException("Could not create PDF Document.", e);
        } finally {
            processQueue.remove(request);
            LOG.trace("Done. Clearing the process queue. New size: " + processQueue.size());
        }
        return file;
    }

    @Override
    public File getPdf(byte[] zipFile) throws PDFCreationFailedException {
        File file = null;
        try {
            File tmpFile = setupTmpFolder();

            File writtenZipFile = new File(tmpFile, "tmp.zip");
            tmpFile.mkdirs();
            LOG.info("Moving zip file to " + tmpFile.getAbsolutePath());
            Files.write(zipFile, writtenZipFile);

            ZipFile zip = new ZipFile(writtenZipFile);
            zip.extractAll(tmpFile.getPath());

            String template = FileUtils.readFileToString(new File(tmpFile, "template.tex"));

            template = processTemplateMedia(template, tmpFile.getParentFile());
            file = process(template, new File(tmpFile, "template.tex"));

        } catch (Exception e) {
            throw new PDFCreationFailedException("Could not create PDF Document.", e);
        }

        return file;
    }

    private File setupTmpFolder() {
        return new File(fileSystemPathConfig.getTemporal() + File.separator + UUID.randomUUID());
    }

    private File process(String template, File tmpFile) throws PDFCreationFailedException, IOException {
        FileUtils.writeStringToFile(tmpFile, template, "UTF-8");
        return generate(tmpFile);
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
                    boolean exitStatus = process.waitFor(generationOptionsProperties.getTimeout(), TimeUnit.SECONDS);

                    if (!exitStatus) {
                        LOG.warn("PDF Creation return non zero exit value: {}", exitStatus);
                        LOG.warn("Output: {}", outputStream);
                        throw new PDFCreationFailedException("Template wrong. Non Zero exit code: " + exitStatus);

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

    private String processTemplateMedia(String template, File tempFolder) {
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
                LOG.warn("Ignoring malformed URL: " + filteredURL);
                LOG.error("Error", ex);
            }

            patchedTemplate = patchedTemplate.replace("img:" + mediaURL, mediaName);
            mediaIndex++;
        }

        return patchedTemplate;
    }

}
