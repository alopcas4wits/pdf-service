package de.wits.pdf.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "path")
public class FileSystemPathProperties {

  private String pdf;

  private String temporal;

  private String pdfLatex;

  public String getPdf() {
    return pdf;
  }

  public void setPdf(String pdf) {
    this.pdf = pdf;
  }

  public String getTemporal() {
    return temporal;
  }

  public void setTemporal(String temporal) {
    this.temporal = temporal;
  }

  public String getPdfLatex() {
    return pdfLatex;
  }

  public void setPdfLatex(String pdfLatex) {
    this.pdfLatex = pdfLatex;
  }

}
