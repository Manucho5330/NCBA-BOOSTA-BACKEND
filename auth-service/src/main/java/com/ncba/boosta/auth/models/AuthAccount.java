package com.ncba.boosta.auth.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_accounts")
public class AuthAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 160)
    private String identifier;
    @Column(nullable = false, length = 30)
    private String role;
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Column(length = 100)
    private String subjectId;
    @Column(name = "admin_marker", unique = true, length = 10)
    private String adminMarker;
    @Column(nullable = false)
    private boolean enabled = true;

    protected AuthAccount() { }

    public AuthAccount(String identifier, String role, String passwordHash, String subjectId) {
        this.identifier = identifier;
        this.role = role;
        this.passwordHash = passwordHash;
        this.subjectId = subjectId;
        this.adminMarker = "ADMIN".equals(role) ? "ADMIN" : null;
    }

    public Long getId() { return id; }
    public String getIdentifier() { return identifier; }
    public String getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }
    public String getSubjectId() { return subjectId; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
