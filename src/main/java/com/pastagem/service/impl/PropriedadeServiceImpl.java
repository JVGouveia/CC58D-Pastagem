package com.pastagem.service.impl;

import com.pastagem.model.Propriedade;
import com.pastagem.model.Pastagem;
import com.pastagem.model.Usuario;
import com.pastagem.repository.PropriedadeRepository;
import com.pastagem.service.PropriedadeService;
import com.pastagem.service.UsuarioService;
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
public class PropriedadeServiceImpl implements PropriedadeService {

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private UsuarioService usuarioService;

    // Operações básicas CRUD
    @Override
    @Transactional(readOnly = true)
    public Page<Propriedade> findAll(Pageable pageable) {
        return propriedadeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Propriedade> findAll() {
        return propriedadeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Propriedade> findById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return propriedadeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return propriedadeRepository.existsById(id);
    }

    @Override
    public Propriedade save(Propriedade propriedade, String userId) {
        if (propriedade == null) {
            throw new IllegalArgumentException("Propriedade não pode ser nula");
        }
        
        // Validações de negócio
        validarPropriedade(propriedade);
        
        // Busca o usuário pelo ID do Cognito
        Usuario usuario = usuarioService.findByCognitoId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Associa o usuário à propriedade
        propriedade.setUsuario(usuario);
        
        return propriedadeRepository.save(propriedade);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID deve ser um número positivo");
        }
        
        if (!existsById(id)) {
            throw new IllegalArgumentException("Propriedade não encontrada com ID: " + id);
        }
        
        // Verificar se há pastagens associadas
        List<Pastagem> pastagens = findPastagensByPropriedadeId(id);
        if (!pastagens.isEmpty()) {
            throw new RuntimeException("Não é possível excluir propriedade que possui pastagens cadastradas");
        }
        
        propriedadeRepository.deleteById(id);
    }

    // Operações específicas de busca
    @Override
    @Transactional(readOnly = true)
    public List<Propriedade> findByUsuarioId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            return List.of();
        }
        return propriedadeRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Propriedade> findByCidade(String cidade) {
        if (cidade == null || cidade.trim().isEmpty()) {
            return List.of();
        }
        return propriedadeRepository.findByCidadeIgnoreCase(cidade.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Propriedade> findByEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return List.of();
        }
        return propriedadeRepository.findByEstadoIgnoreCase(estado.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Propriedade> findByNomeContaining(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return List.of();
        }
        return propriedadeRepository.findByNomeContainingIgnoreCase(nome.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Propriedade> findByCidadeAndEstado(String cidade, String estado) {
        if (cidade == null || cidade.trim().isEmpty() || estado == null || estado.trim().isEmpty()) {
            return List.of();
        }
        return propriedadeRepository.findByCidadeIgnoreCaseAndEstadoIgnoreCase(cidade.trim(), estado.trim());
    }

    // Operações relacionadas
    @Override
    @Transactional(readOnly = true)
    public List<Pastagem> findPastagensByPropriedadeId(Long propriedadeId) {
        if (propriedadeId == null || propriedadeId <= 0) {
            return List.of();
        }
        return propriedadeRepository.findPastagensByPropriedadeId(propriedadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPastagensByPropriedadeId(Long propriedadeId) {
        if (propriedadeId == null || propriedadeId <= 0) {
            return 0;
        }
        return propriedadeRepository.countPastagensByPropriedadeId(propriedadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularAreaTotalPastagens(Long propriedadeId) {
        if (propriedadeId == null || propriedadeId <= 0) {
            return BigDecimal.ZERO;
        }
        
        List<Pastagem> pastagens = findPastagensByPropriedadeId(propriedadeId);
        return pastagens.stream()
                .map(Pastagem::getAreaHectares)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Validações de negócio
    private void validarPropriedade(Propriedade propriedade) {
        if (propriedade.getNome() == null || propriedade.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da propriedade é obrigatório");
        }
        
        if (propriedade.getEndereco() == null || propriedade.getEndereco().trim().isEmpty()) {
            throw new IllegalArgumentException("Endereço é obrigatório");
        }
        
        if (propriedade.getCidade() == null || propriedade.getCidade().trim().isEmpty()) {
            throw new IllegalArgumentException("Cidade é obrigatória");
        }
        
        if (propriedade.getEstado() == null || propriedade.getEstado().trim().isEmpty()) {
            throw new IllegalArgumentException("Estado é obrigatório");
        }
        
        if (propriedade.getUsuario() == null || propriedade.getUsuario().getId() == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }
        
        // Verificar se o usuário existe
        if (!usuarioService.existsById(propriedade.getUsuario().getId())) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
    }
}