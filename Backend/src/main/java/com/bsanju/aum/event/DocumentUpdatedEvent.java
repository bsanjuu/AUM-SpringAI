package com.bsanju.aum.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a document is added, updated, or indexed.
 * Can be used to trigger reindexing, cache invalidation, or notifications.
 */
public class DocumentUpdatedEvent extends ApplicationEvent {

    private final Long documentId;
    private final String action; // "CREATED", "UPDATED", "DELETED", "INDEXED"

    public DocumentUpdatedEvent(Object source, Long documentId) {
        this(source, documentId, "UPDATED");
    }

    public DocumentUpdatedEvent(Object source, Long documentId, String action) {
        super(source);
        this.documentId = documentId;
        this.action = action;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "DocumentUpdatedEvent{" +
                "documentId=" + documentId +
                ", action='" + action + '\'' +
                '}';
    }
}
