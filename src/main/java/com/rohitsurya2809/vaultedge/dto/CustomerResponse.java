package com.rohitsurya2809.vaultedge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    
    
}
