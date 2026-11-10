package com.ncba.boosta.anchor.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AnchorOnboardingDocument {
    @Column(name = "document_type")
    private String documentType;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "document_reference")
    private String reference;
    @Column(name = "recorded_at")
    private String recordedAt;

    protected AnchorOnboardingDocument() {
    }

    public AnchorOnboardingDocument(String documentType, String fileName, String reference, String recordedAt) {
        this.documentType = documentType;
        this.fileName = fileName;
        this.reference = reference;
        this.recordedAt = recordedAt;
    }

    public String getDocumentType() { return documentType; }
    public String getFileName() { return fileName; }
    public String getReference() { return reference; }
    public String getRecordedAt() { return recordedAt; }
}
