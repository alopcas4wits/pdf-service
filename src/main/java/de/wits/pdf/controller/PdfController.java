package de.wits.pdf.controller;

import de.wits.pdf.service.PdfService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdfController {

    @Autowired
    PdfService pdfService;

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);

    @RequestMapping(value = "/pdf", method = RequestMethod.POST, consumes = "text/plain")
    public ResponseEntity<byte[]> getPdfFile(@RequestBody String template) throws IOException {
        if (template == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.debug("Generating PDF file");
        File pdfFile;

        try {
            pdfFile = pdfService.getPdf(template);
            log.trace("Returning pdf file {} stored at {}", pdfFile.getName(), pdfFile.getAbsolutePath());
            //resource = Files.readAllBytes(pdfService.getPdf(template).toPath());
        } catch (Exception e) {
            log.error("Exception occurred while generating the PDF file", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Returning file");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(Files.readAllBytes(pdfFile.toPath()), headers, HttpStatus.OK);
    }
}
