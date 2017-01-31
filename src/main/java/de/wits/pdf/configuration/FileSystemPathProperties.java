package de.wits.pdf.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pdf")
public class FileSystemPathProperties {

  private String temporal;

  private String pdfLatex;

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
