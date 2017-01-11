package de.wits.pdf.controller;

import de.wits.pdf.service.PdfService;
import java.io.File;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
  public ResponseEntity<byte[]> getPdfFile(@RequestBody String template) {
    if (template == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    log.debug("Generating PDF file");
    File pdfFile;
    byte[] resource = null;

    try {
      pdfFile = pdfService.getPdf(template);
      log.trace("Returning pdf file {} stored at {}", pdfFile.getName(), pdfFile.getAbsolutePath());
      resource = Files.readAllBytes(pdfService.getPdf(template).toPath());
    } catch (Exception e) {
      log.error("Exception occurred while generating the PDF file", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    log.info("Returning file");

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }
}
