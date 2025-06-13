package com.pastagem.service.impl;

import com.pastagem.model.Pastagem;
import com.pastagem.repository.PastagemRepository;
import com.pastagem.service.PastagemService;
import com.pastagem.service.PropriedadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PastagemServiceImpl implements PastagemService {

    @Autowired
    private PastagemRepository pastagemRepository;

    @Autowired
    private PropriedadeService propriedadeService;

    // Operações básicas CRUD
    @Override
    @Transactional(readOnly = true)
    public Page<Pastagem> findAll(Pageable pageable) {
        return pastagemRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pastagem> findAll() {
        return pastagemRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pastagem> findById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return pastagemRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return pastagemRepository.existsById(id);
    }

    @Override
    public Pastagem save(Pastagem pastagem) {
        if (pastagem == null) {
            throw new IllegalArgumentException("Pastagem não pode ser nula");
        }
        
        // Validações de negócio
        validarPastagem(pastagem);
        
        return pastagemRepository.save(pastagem);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID deve ser um número positivo");
        }
        
        if (!existsById(id)) {
            throw new IllegalArgumentException("Pastagem não encontrada com ID: " + id);
        }
        
        pastagemRepository.deleteById(id);
    }

    // Operações específicas de busca
    @Override
    @Transactional(readOnly = true)
    public List<Pastagem> findByPropriedadeId(Long propriedadeId) {
        if (propriedadeId == null || propriedadeId <= 0) {
            return List.of();
        }
        return pastagemRepository.findByPropriedadeId(propriedadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pastagem> findByTipoPasto(String tipoPasto) {
        if (tipoPasto == null || tipoPasto.trim().isEmpty()) {
            return List.of();
        }
        return pastagemRepository.findByTipoPastoIgnoreCase(tipoPasto.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pastagem> findByNomeContaining(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return List.of();
        }
        return pastagemRepository.findByNomeContainingIgnoreCase(nome.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pastagem> findByAreaHectaresBetween(BigDecimal areaMin, BigDecimal areaMax) {
        if (areaMin == null || areaMax == null || areaMin.compareTo(BigDecimal.ZERO) < 0 || areaMax.compareTo(areaMin) < 0) {
            return List.of();
        }
        return pastagemRepository.findByAreaHectaresBetween(areaMin, areaMax);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pastagem> findByCapacidadeSuporteBetween(Integer capacidadeMin, Integer capacidadeMax) {
        if (capacidadeMin == null || capacidadeMax == null || capacidadeMin <= 0 || capacidadeMax < capacidadeMin) {
            return List.of();
        }
        return pastagemRepository.findByCapacidadeSuporteBetween(capacidadeMin, capacidadeMax);
    }

    // Operações de relatório e análise
    @Override
    @Transactional(readOnly = true)
    public List<Pastagem> findPastagensComCapacidadeInadequada() {
        return pastagemRepository.findAll().stream()
                .filter(pastagem -> !pastagem.isCapacidadeAdequada())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularAreaTotalPorPropriedade(Long propriedadeId) {
        if (propriedadeId == null || propriedadeId <= 0) {
            return BigDecimal.ZERO;
        }
        
        List<Pastagem> pastagens = findByPropriedadeId(propriedadeId);
        return pastagens.stream()
                .map(Pastagem::getAreaHectares)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calcularCapacidadeTotalPorPropriedade(Long propriedadeId) {
        if (propriedadeId == null || propriedadeId <= 0) {
            return 0;
        }
        
        List<Pastagem> pastagens = findByPropriedadeId(propriedadeId);
        return pastagens.stream()
                .mapToInt(Pastagem::getCapacidadeSuporte)
                .sum();
    }

    @SuppressWarnings("deprecation")
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularDensidadeMediaPorPropriedade(Long propriedadeId) {
        if (propriedadeId == null || propriedadeId <= 0) {
            return BigDecimal.ZERO;
        }
        
        List<Pastagem> pastagens = findByPropriedadeId(propriedadeId);
        if (pastagens.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal somaDensidades = pastagens.stream()
                .map(Pastagem::calcularDensidadePorHectare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return somaDensidades.divide(BigDecimal.valueOf(pastagens.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> relatorioResumoPropriedades() {
        return pastagemRepository.findResumoPropriedades();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> relatorioPorTipoPasto() {
        return pastagemRepository.findRelatorioPorTipoPasto();
    }

    // Validações de negócio
    private void validarPastagem(Pastagem pastagem) {
        if (pastagem.getNome() == null || pastagem.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da pastagem é obrigatório");
        }
        
        if (pastagem.getAreaHectares() == null || pastagem.getAreaHectares().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Área deve ser maior que zero");
        }
        
        if (pastagem.getTipoPasto() == null || pastagem.getTipoPasto().trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de pasto é obrigatório");
        }
        
        if (pastagem.getCapacidadeSuporte() == null || pastagem.getCapacidadeSuporte() <= 0) {
            throw new IllegalArgumentException("Capacidade de suporte deve ser maior que zero");
        }
        
        if (pastagem.getPropriedade() == null || pastagem.getPropriedade().getId() == null) {
            throw new IllegalArgumentException("Propriedade é obrigatória");
        }
        
        // Verificar se a propriedade existe
        if (!propriedadeService.existsById(pastagem.getPropriedade().getId())) {
            throw new IllegalArgumentException("Propriedade não encontrada");
        }
        
        // Validação de densidade extremamente alta (mais de 10 animais por hectare)
        BigDecimal densidadeMaxima = BigDecimal.valueOf(10.0);
        if (pastagem.calcularDensidadePorHectare().compareTo(densidadeMaxima) > 0) {
            throw new IllegalArgumentException("Densidade muito alta: " + 
                pastagem.calcularDensidadePorHectare() + " animais/hectare. Máximo recomendado: " + densidadeMaxima);
        }
    }
}