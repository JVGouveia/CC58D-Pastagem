package com.pastagem.repository;

import com.pastagem.model.Propriedade;
import com.pastagem.model.Pastagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropriedadeRepository extends JpaRepository<Propriedade, Long> {

    // Busca por usuário
    List<Propriedade> findByUsuarioId(Long usuarioId);
    Page<Propriedade> findByUsuarioId(Long usuarioId, Pageable pageable);
    Optional<Propriedade> findByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
    
    // Busca por cidade (case-insensitive)
    List<Propriedade> findByCidadeIgnoreCase(String cidade);
    List<Propriedade> findByUsuarioIdAndCidadeIgnoreCase(Long usuarioId, String cidade);
    
    // Busca por estado (case-insensitive)
    List<Propriedade> findByEstadoIgnoreCase(String estado);
    List<Propriedade> findByUsuarioIdAndEstadoIgnoreCase(Long usuarioId, String estado);
    
    // Busca por nome contendo (case-insensitive)
    List<Propriedade> findByNomeContainingIgnoreCase(String nome);
    List<Propriedade> findByUsuarioIdAndNomeContainingIgnoreCase(Long usuarioId, String nome);
    
    // Busca por cidade e estado (case-insensitive)
    List<Propriedade> findByCidadeIgnoreCaseAndEstadoIgnoreCase(String cidade, String estado);
    List<Propriedade> findByUsuarioIdAndCidadeIgnoreCaseAndEstadoIgnoreCase(Long usuarioId, String cidade, String estado);
    
    // Query personalizada para buscar pastagens de uma propriedade
    @Query("SELECT p.pastagens FROM Propriedade p WHERE p.id = :propriedadeId")
    List<Pastagem> findPastagensByPropriedadeId(@Param("propriedadeId") Long propriedadeId);
    
    // Query personalizada para contar pastagens de uma propriedade
    @Query("SELECT COUNT(p) FROM Pastagem p WHERE p.propriedade.id = :propriedadeId")
    long countPastagensByPropriedadeId(@Param("propriedadeId") Long propriedadeId);
    
    @Query("SELECT COALESCE(SUM(p.areaHectares), 0) FROM Pastagem p WHERE p.propriedade.id = :propriedadeId")
    BigDecimal calcularAreaTotalPastagens(@Param("propriedadeId") Long propriedadeId);
}