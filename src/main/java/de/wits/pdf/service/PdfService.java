package de.wits.pdf.service;

import de.wits.pdf.model.PdfRequest;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface PdfService {

    public CompletableFuture<File> getPdf(PdfRequest template) throws PDFCreationFailedException, ExecutionException, InterruptedException;

    public File getPdf(byte[] template) throws PDFCreationFailedException;

}
