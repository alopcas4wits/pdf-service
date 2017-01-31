package de.wits.pdf.service;

import java.io.File;

public interface PdfService {

  public File getPdf(String template) throws PDFCreationFailedException;

}
