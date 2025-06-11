package com.pastagem.dto;

import lombok.Data;

@Data
public class UsuarioRegisterDTO {
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String password;
    private String cargo;
} 