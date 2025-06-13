package com.pastagem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.math.BigDecimal;

@Entity
@Table(name = "pastagem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pastagem extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 100)
    @NotBlank(message = "Nome da pastagem é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Column(name = "areaHectares", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Área em hectares é obrigatória")
    @DecimalMin(value = "0.01", message = "Área deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Área deve ter no máximo 8 dígitos inteiros e 2 decimais")
    private BigDecimal areaHectares;

    @Column(name = "tipoPasto", nullable = false, length = 100)
    @NotBlank(message = "Tipo de pasto é obrigatório")
    @Size(max = 100, message = "Tipo de pasto deve ter no máximo 100 caracteres")
    private String tipoPasto;

    @Column(name = "capacidadeSuporte", nullable = false)
    @NotNull(message = "Capacidade de suporte é obrigatória")
    @Min(value = 1, message = "Capacidade de suporte deve ser pelo menos 1")
    @Max(value = 10000, message = "Capacidade de suporte não pode exceder 10.000")
    private Integer capacidadeSuporte;

    // Relacionamento Many-to-One com Propriedade
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_propriedade", nullable = false, foreignKey = @ForeignKey(name = "FK_Pastagem_Propriedade"))
    @NotNull(message = "Propriedade é obrigatória")
    @JsonBackReference
    private Propriedade propriedade;

    @PrePersist
    @PreUpdate
    private void validateData() {
        if (nome != null) {
            nome = nome.trim();
        }
        if (tipoPasto != null) {
            tipoPasto = tipoPasto.trim();
        }
        
        // Validação adicional para área
        if (areaHectares != null && areaHectares.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Área deve ser maior que zero");
        }
        
        // Validação adicional para capacidade
        if (capacidadeSuporte != null && capacidadeSuporte <= 0) {
            throw new IllegalArgumentException("Capacidade de suporte deve ser maior que zero");
        }
    }

    // Métodos de conveniência para cálculos
    @SuppressWarnings("deprecation")
    public BigDecimal calcularDensidadePorHectare() {
        if (areaHectares != null && areaHectares.compareTo(BigDecimal.ZERO) > 0 && capacidadeSuporte != null) {
            return BigDecimal.valueOf(capacidadeSuporte).divide(areaHectares, 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public boolean isCapacidadeAdequada() {
        BigDecimal densidade = calcularDensidadePorHectare();
        // Considerando uma densidade máxima recomendada de 2 animais por hectare
        return densidade.compareTo(BigDecimal.valueOf(2.0)) <= 0;
    }
}