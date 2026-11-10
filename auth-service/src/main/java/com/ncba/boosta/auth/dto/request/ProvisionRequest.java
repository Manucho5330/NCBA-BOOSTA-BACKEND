package com.ncba.boosta.auth.dto.request;

public record ProvisionRequest(String identifier, String secret, String role, String subjectId) {
}
