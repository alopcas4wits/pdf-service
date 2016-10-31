package de.wits.pdf.exception;

/**
 * Created by User on 14.07.2016.
 */
public class PDFCreationFailedException extends Exception {

  public PDFCreationFailedException(String s) {
    super(s);
  }

  public PDFCreationFailedException(String s, Exception e) {
    super(s, e);
  }
}
