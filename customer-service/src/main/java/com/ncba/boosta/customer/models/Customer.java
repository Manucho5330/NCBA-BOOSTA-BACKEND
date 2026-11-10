package com.ncba.boosta.customer.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    private String id;
    private String fullName;
    private String phone;
    private String email;
    private String ncbaAccountNumber;
    private String nationalId;
    private String businessName;
    private String businessType;
    private String kycStatus;
    private String memberSince;
    @ElementCollection
    private List<String> prefilledFields = new ArrayList<>();

    public Customer() {
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getNcbaAccountNumber() { return ncbaAccountNumber; }
    public String getNationalId() { return nationalId; }
    public String getBusinessName() { return businessName; }
    public String getBusinessType() { return businessType; }
    public String getKycStatus() { return kycStatus; }
    public String getMemberSince() { return memberSince; }
    public List<String> getPrefilledFields() { return List.copyOf(prefilledFields); }
    public void setId(String id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setNcbaAccountNumber(String ncbaAccountNumber) { this.ncbaAccountNumber = ncbaAccountNumber; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }
    public void setMemberSince(String memberSince) { this.memberSince = memberSince; }
    public void setPrefilledFields(List<String> prefilledFields) {
        this.prefilledFields = new ArrayList<>(prefilledFields == null ? List.of() : prefilledFields);
    }
}
