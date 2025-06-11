package com.pastagem.service;

import com.pastagem.model.Usuario;
import com.pastagem.model.Propriedade;
import com.pastagem.model.Cargo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    // Operações básicas CRUD
    Page<Usuario> findAll(Pageable pageable);
    List<Usuario> findAll();
    Optional<Usuario> findById(Long id);
    boolean existsById(Long id);
    Usuario save(Usuario usuario);
    void deleteById(Long id);

    // Operações específicas de busca
    Optional<Usuario> findByCpf(String cpf);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByCognitoId(String cognitoId);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    List<Usuario> findByNomeContaining(String nome);
    List<Usuario> findByCargo(Cargo cargo);

    // Operações relacionadas
    List<Propriedade> findPropriedadesByUsuarioId(Long usuarioId);
    long countPropriedadesByUsuarioId(Long usuarioId);
}