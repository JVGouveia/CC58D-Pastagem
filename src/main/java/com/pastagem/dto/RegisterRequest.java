package com.pastagem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String password;
    private String cargo;
} 