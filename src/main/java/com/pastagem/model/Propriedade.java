package com.pastagem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "propriedade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Propriedade extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 100)
    @NotBlank(message = "Nome da propriedade é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Column(name = "endereco", nullable = false, length = 200)
    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    private String endereco;

    @Column(name = "area_total", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Área total é obrigatória")
    @DecimalMin(value = "0.01", message = "Área total deve ser maior que zero")
    private BigDecimal areaTotal;

    @Column(name = "cidade", nullable = false, length = 100)
    @NotBlank(message = "Cidade é obrigatória")
    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @Column(name = "estado", nullable = false, length = 50)
    @NotBlank(message = "Estado é obrigatório")
    @Size(max = 50, message = "Estado deve ter no máximo 50 caracteres")
    private String estado;

    // Relacionamento Many-to-One com Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, foreignKey = @ForeignKey(name = "FK_Propriedade_Usuario"))
    @NotNull(message = "Usuário é obrigatório")
    @JsonBackReference
    private Usuario usuario;

    // Relacionamento One-to-Many com Pastagem
    @OneToMany(mappedBy = "propriedade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Pastagem> pastagens;

    @PrePersist
    @PreUpdate
    private void validateData() {
        if (nome != null) {
            nome = nome.trim();
        }
        if (endereco != null) {
            endereco = endereco.trim();
        }
        if (cidade != null) {
            cidade = cidade.trim();
        }
        if (estado != null) {
            estado = estado.trim().toUpperCase();
        }
    }

    // Método de conveniência para obter endereço completo
    public String getEnderecoCompleto() {
        return String.format("%s, %s - %s", endereco, cidade, estado);
    }
}