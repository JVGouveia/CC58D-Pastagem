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

    // Novos métodos com id_usuario
    Page<Propriedade> findByUsuarioId(Long usuarioId, Pageable pageable);
    Optional<Propriedade> findByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
    List<Propriedade> findByUsuarioIdAndCidade(Long usuarioId, String cidade);
    List<Propriedade> findByUsuarioIdAndEstado(Long usuarioId, String estado);
    List<Propriedade> findByUsuarioIdAndNomeContaining(Long usuarioId, String nome);
    List<Propriedade> findByUsuarioIdAndCidadeAndEstado(Long usuarioId, String cidade, String estado);

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