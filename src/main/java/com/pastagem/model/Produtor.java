package com.pastagem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produtores")
@Getter
@Setter
@ToString
public class Produtor extends BaseEntity {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "produtor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Propriedade> propriedades = new ArrayList<>();
} 