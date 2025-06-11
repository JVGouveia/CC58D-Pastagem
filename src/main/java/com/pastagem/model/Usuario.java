package com.pastagem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 100)
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Column(name = "cpf", nullable = false, unique = true, length = 14)
    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @Column(name = "telefone", nullable = false, length = 20)
    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo", nullable = false)
    @NotNull(message = "Cargo é obrigatório")
    private Cargo cargo;

    @Column(name = "cognitoId", unique = true)
    private String cognitoId;

    // Relacionamento One-to-Many com Propriedade
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Propriedade> propriedades;

    // Métodos de conveniência para validação
    public boolean isValidCpf() {
        return cpf != null && cpf.matches("\\d{11}");
    }

    public boolean isValidEmail() {
        return email != null && email.contains("@") && email.contains(".");
    }

    @PrePersist
    @PreUpdate
    private void validateData() {
        if (nome != null) {
            nome = nome.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (cpf != null) {
            cpf = cpf.replaceAll("[^0-9]", "");
        }
        if (telefone != null) {
            telefone = telefone.replaceAll("[^0-9]", "");
        }
    }
}