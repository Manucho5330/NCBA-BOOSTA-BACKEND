package com.ncba.boosta.customer.dto.request;

import com.ncba.boosta.customer.models.Customer;
import java.util.List;

public record CustomerRequest(String id, String fullName, String phone, String email,
                              String ncbaAccountNumber, String nationalId, String businessName,
                              String businessType, String kycStatus, String memberSince,
                              List<String> prefilledFields) {
    public Customer toModel() {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setNcbaAccountNumber(ncbaAccountNumber);
        customer.setNationalId(nationalId);
        customer.setBusinessName(businessName);
        customer.setBusinessType(businessType);
        customer.setKycStatus(kycStatus);
        customer.setMemberSince(memberSince);
        customer.setPrefilledFields(prefilledFields);
        return customer;
    }
}
