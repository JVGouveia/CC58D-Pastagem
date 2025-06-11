package com.pastagem.service;

import com.pastagem.model.Pastagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PastagemService {

    // Operações básicas CRUD
    Page<Pastagem> findAll(Pageable pageable);
    List<Pastagem> findAll();
    Optional<Pastagem> findById(Long id);
    boolean existsById(Long id);
    Pastagem save(Pastagem pastagem);
    void deleteById(Long id);

    // Operações específicas de busca
    List<Pastagem> findByPropriedadeId(Long propriedadeId);
    List<Pastagem> findByTipoPasto(String tipoPasto);
    List<Pastagem> findByNomeContaining(String nome);
    List<Pastagem> findByAreaHectaresBetween(BigDecimal areaMin, BigDecimal areaMax);
    List<Pastagem> findByCapacidadeSuporteBetween(Integer capacidadeMin, Integer capacidadeMax);

    // Operações de relatório e análise
    List<Pastagem> findPastagensComCapacidadeInadequada();
    BigDecimal calcularAreaTotalPorPropriedade(Long propriedadeId);
    Integer calcularCapacidadeTotalPorPropriedade(Long propriedadeId);
    BigDecimal calcularDensidadeMediaPorPropriedade(Long propriedadeId);
    List<Object[]> relatorioResumoPropriedades();
    List<Object[]> relatorioPorTipoPasto();
}