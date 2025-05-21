package com.pastagem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "areas_pastagem")
@Getter
@Setter
@ToString
public class AreaPastagem extends BaseEntity {

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "A área em hectares é obrigatória")
    @Positive(message = "A área deve ser um número positivo")
    @Column(nullable = false)
    private Double areaHectares;

    @NotBlank(message = "O tipo de pasto é obrigatório")
    @Column(nullable = false)
    private String tipoPasto;

    @NotNull(message = "A capacidade de suporte é obrigatória")
    @Positive(message = "A capacidade de suporte deve ser um número positivo")
    @Column(nullable = false)
    private Integer capacidadeSuporte;

    @ManyToOne
    @JoinColumn(name = "propriedade_id", nullable = false)
    //
    private Propriedade propriedade;
} 