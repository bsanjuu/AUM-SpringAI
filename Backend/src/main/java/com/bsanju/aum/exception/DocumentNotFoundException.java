package com.bsanju.aum.exception;

/**
 * Exception thrown when a requested document is not found.
 */
public class DocumentNotFoundException extends RuntimeException {

    private final Long documentId;

    public DocumentNotFoundException(Long documentId) {
        super("Document not found with ID: " + documentId);
        this.documentId = documentId;
    }

    public DocumentNotFoundException(String message) {
        super(message);
        this.documentId = null;
    }

    public DocumentNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.documentId = null;
    }

    public Long getDocumentId() {
        return documentId;
    }
}
