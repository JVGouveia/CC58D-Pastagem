package com.pastagem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "areas_pastagem")
@Getter
@Setter
@ToString
public class AreaPastagem extends BaseEntity {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Double areaHectares;

    @Column(nullable = false)
    private String tipoPasto;

    @Column(nullable = false)
    private Integer capacidadeSuporte;

    @ManyToOne
    @JoinColumn(name = "propriedade_id", nullable = false)
    private Propriedade propriedade;
} 