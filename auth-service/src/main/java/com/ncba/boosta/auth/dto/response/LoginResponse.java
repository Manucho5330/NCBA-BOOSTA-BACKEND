package com.ncba.boosta.auth.dto.response;

import java.time.Instant;

public record LoginResponse(String token, String role, String subjectId, Instant expiresAt) {
}
