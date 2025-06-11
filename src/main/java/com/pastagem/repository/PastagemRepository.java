package com.pastagem.repository;

import com.pastagem.model.Pastagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PastagemRepository extends JpaRepository<Pastagem, Long> {

    // Busca por propriedade
    List<Pastagem> findByPropriedadeId(Long propriedadeId);
    
    // Busca por tipo de pasto (case-insensitive)
    List<Pastagem> findByTipoPastoIgnoreCase(String tipoPasto);
    
    // Busca por nome contendo (case-insensitive)
    List<Pastagem> findByNomeContainingIgnoreCase(String nome);
    
    // Busca por área entre valores
    List<Pastagem> findByAreaHectaresBetween(BigDecimal areaMin, BigDecimal areaMax);
    
    // Busca por capacidade de suporte entre valores
    List<Pastagem> findByCapacidadeSuporteBetween(Integer capacidadeMin, Integer capacidadeMax);
    
    // Query personalizada para relatório resumo de propriedades
    @Query("""
        SELECT 
            prop.id as propriedadeId,
            prop.nome as propriedadeNome,
            prop.cidade,
            prop.estado,
            u.nome as proprietarioNome,
            COUNT(p.id) as totalPastagens,
            COALESCE(SUM(p.areaHectares), 0) as areaTotal,
            COALESCE(SUM(p.capacidadeSuporte), 0) as capacidadeTotal
        FROM Pastagem p 
        RIGHT JOIN p.propriedade prop
        LEFT JOIN prop.usuario u
        GROUP BY prop.id, prop.nome, prop.cidade, prop.estado, u.nome
        ORDER BY prop.nome
        """)
    List<Object[]> findResumoPropriedades();
    
    // Query personalizada para relatório por tipo de pasto
    @Query("""
        SELECT 
            p.tipoPasto,
            COUNT(p.id) as quantidade,
            COALESCE(SUM(p.areaHectares), 0) as areaTotal,
            COALESCE(SUM(p.capacidadeSuporte), 0) as capacidadeTotal,
            COALESCE(AVG(p.capacidadeSuporte / p.areaHectares), 0) as densidadeMedia
        FROM Pastagem p
        GROUP BY p.tipoPasto
        ORDER BY areaTotal DESC
        """)
    List<Object[]> findRelatorioPorTipoPasto();
    
    // Query para buscar pastagens por faixa de densidade
    @Query("""
        SELECT p FROM Pastagem p 
        WHERE (p.capacidadeSuporte / p.areaHectares) BETWEEN :densidadeMin AND :densidadeMax
        ORDER BY (p.capacidadeSuporte / p.areaHectares) DESC
        """)
    List<Pastagem> findByDensidadeBetween(
        @Param("densidadeMin") BigDecimal densidadeMin, 
        @Param("densidadeMax") BigDecimal densidadeMax
    );
    
    // Query para buscar pastagens com maior densidade por propriedade
    @Query("""
        SELECT p FROM Pastagem p
        WHERE p.propriedade.id = :propriedadeId
        ORDER BY (p.capacidadeSuporte / p.areaHectares) DESC
        """)
    List<Pastagem> findByPropriedadeIdOrderByDensidadeDesc(@Param("propriedadeId") Long propriedadeId);
    
    // Query para estatísticas gerais
    @Query("""
        SELECT 
            COUNT(p.id) as totalPastagens,
            COALESCE(SUM(p.areaHectares), 0) as areaTotal,
            COALESCE(SUM(p.capacidadeSuporte), 0) as capacidadeTotal,
            COALESCE(AVG(p.areaHectares), 0) as areaMedia,
            COALESCE(AVG(p.capacidadeSuporte), 0) as capacidadeMedia,
            COALESCE(AVG(p.capacidadeSuporte / p.areaHectares), 0) as densidadeMedia
        FROM Pastagem p
        """)
    Object[] findEstatisticasGerais();
}