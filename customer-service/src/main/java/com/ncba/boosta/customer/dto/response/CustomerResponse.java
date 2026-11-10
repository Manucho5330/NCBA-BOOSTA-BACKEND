package com.ncba.boosta.customer.dto.response;

import com.ncba.boosta.customer.models.Customer;
import java.util.List;

public record CustomerResponse(String id, String fullName, String phone, String email,
                               String ncbaAccountNumber, String nationalId, String businessName,
                               String businessType, String kycStatus, String memberSince,
                               List<String> prefilledFields) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getFullName(), customer.getPhone(),
                customer.getEmail(), customer.getNcbaAccountNumber(), customer.getNationalId(),
                customer.getBusinessName(), customer.getBusinessType(), customer.getKycStatus(),
                customer.getMemberSince(), customer.getPrefilledFields());
    }
}
