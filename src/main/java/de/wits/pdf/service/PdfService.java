package de.wits.pdf.service;

import de.wits.pdf.exception.PDFCreationFailedException;
import java.io.File;

public interface PdfService {

  public File getPdf(String template) throws PDFCreationFailedException;

}
