package com.pastagem.service.impl;

import com.pastagem.model.Usuario;
import com.pastagem.model.Propriedade;
import com.pastagem.model.Cargo;
import com.pastagem.repository.UsuarioRepository;
import com.pastagem.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Operações básicas CRUD
    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return usuarioRepository.existsById(id);
    }

    @Override
    public Usuario save(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        
        // Validações de negócio
        validarUsuario(usuario);
        
        return usuarioRepository.save(usuario);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID deve ser um número positivo");
        }
        
        if (!existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + id);
        }
        
        // Verificar se há propriedades associadas
        List<Propriedade> propriedades = findPropriedadesByUsuarioId(id);
        if (!propriedades.isEmpty()) {
            throw new RuntimeException("Não é possível excluir usuário que possui propriedades cadastradas");
        }
        
        usuarioRepository.deleteById(id);
    }

    // Operações específicas de busca
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return Optional.empty();
        }
        return usuarioRepository.findByCpf(cpf.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmailIgnoreCase(email.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByCognitoId(String cognitoId) {
        if (cognitoId == null || cognitoId.trim().isEmpty()) {
            return Optional.empty();
        }
        return usuarioRepository.findByCognitoId(cognitoId.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }
        return usuarioRepository.existsByCpf(cpf.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return usuarioRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findByNomeContaining(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return List.of();
        }
        return usuarioRepository.findByNomeContainingIgnoreCase(nome.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findByCargo(Cargo cargo) {
        if (cargo == null) {
            return List.of();
        }
        return usuarioRepository.findByCargo(cargo);
    }

    // Operações relacionadas
    @Override
    @Transactional(readOnly = true)
    public List<Propriedade> findPropriedadesByUsuarioId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            return List.of();
        }
        return usuarioRepository.findPropriedadesByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPropriedadesByUsuarioId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            return 0;
        }
        return usuarioRepository.countPropriedadesByUsuarioId(usuarioId);
    }

    // Validações de negócio
    private void validarUsuario(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        if (usuario.getCpf() == null || !usuario.isValidCpf()) {
            throw new IllegalArgumentException("CPF inválido");
        }
        
        if (usuario.getEmail() == null || !usuario.isValidEmail()) {
            throw new IllegalArgumentException("Email inválido");
        }
        
        // Verificar duplicidade apenas para novos usuários ou alteração de dados únicos
        if (usuario.getId() == null) {
            if (existsByCpf(usuario.getCpf())) {
                throw new IllegalArgumentException("CPF já cadastrado");
            }
            if (existsByEmail(usuario.getEmail())) {
                throw new IllegalArgumentException("Email já cadastrado");
            }
        } else {
            // Para atualizações, verificar se CPF/email não pertencem a outro usuário
            Optional<Usuario> usuarioExistenteCpf = findByCpf(usuario.getCpf());
            if (usuarioExistenteCpf.isPresent() && !usuarioExistenteCpf.get().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("CPF já cadastrado para outro usuário");
            }
            
            Optional<Usuario> usuarioExistenteEmail = findByEmail(usuario.getEmail());
            if (usuarioExistenteEmail.isPresent() && !usuarioExistenteEmail.get().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("Email já cadastrado para outro usuário");
            }
        }
    }
}