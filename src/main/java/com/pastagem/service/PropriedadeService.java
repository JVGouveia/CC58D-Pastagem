package com.pastagem.service;

import com.pastagem.model.Propriedade;
import com.pastagem.model.Pastagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PropriedadeService {
    // Operações básicas CRUD
    Page<Propriedade> findAll(Pageable pageable);
    List<Propriedade> findAll();
    Optional<Propriedade> findById(Long id);
    boolean existsById(Long id);
    Propriedade save(Propriedade propriedade, String userId);
    void deleteById(Long id);

    // Operações específicas de busca
    List<Propriedade> findByUsuarioId(Long usuarioId);
    List<Propriedade> findByCidade(String cidade);
    List<Propriedade> findByEstado(String estado);
    List<Propriedade> findByNomeContaining(String nome);
    List<Propriedade> findByCidadeAndEstado(String cidade, String estado);

    // Operações relacionadas
    List<Pastagem> findPastagensByPropriedadeId(Long propriedadeId);
    long countPastagensByPropriedadeId(Long propriedadeId);
    BigDecimal calcularAreaTotalPastagens(Long propriedadeId);
}