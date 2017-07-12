package de.wits.pdf.model;

import java.io.File;
import java.util.UUID;

/**
 * Created by alberto on 12.07.17.
 */
public class PdfRequest {
    UUID id;
    String latexTemplate;

    File tmpFolder;

    public PdfRequest(UUID id, String latexTemplate) {
        this.id = id;
        this.latexTemplate = latexTemplate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLatexTemplate() {
        return latexTemplate;
    }

    public void setLatexTemplate(String latexTemplate) {
        this.latexTemplate = latexTemplate;
    }

    public File getTmpFolder() {
        return tmpFolder;
    }

    public void setTmpFolder(File tmpFolder) {
        this.tmpFolder = tmpFolder;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PdfRequest that = (PdfRequest) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
