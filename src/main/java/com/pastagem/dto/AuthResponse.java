package com.pastagem.dto;

import com.pastagem.model.Cargo;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class AuthResponse {
    private String idToken;
    private String accessToken;
    private String refreshToken;
    private Cargo cargo;
    private String nome;
    private String email;
    private String cognitoId;
    private Long id;
} 