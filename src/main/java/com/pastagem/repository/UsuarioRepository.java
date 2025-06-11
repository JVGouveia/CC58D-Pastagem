package com.pastagem.repository;

import com.pastagem.model.Usuario;
import com.pastagem.model.Propriedade;
import com.pastagem.model.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca por CPF
    Optional<Usuario> findByCpf(String cpf);
    
    // Verificar existência por CPF
    boolean existsByCpf(String cpf);
    
    // Busca por email (case-insensitive)
    Optional<Usuario> findByEmailIgnoreCase(String email);
    
    // Verificar existência por email (case-insensitive)
    boolean existsByEmailIgnoreCase(String email);
    
    // Busca por nome contendo (case-insensitive)
    List<Usuario> findByNomeContainingIgnoreCase(String nome);
    
    // Busca por cargo
    List<Usuario> findByCargo(Cargo cargo);
    
    // Query personalizada para buscar propriedades de um usuário
    @Query("SELECT p FROM Propriedade p WHERE p.usuario.id = :usuarioId")
    List<Propriedade> findPropriedadesByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    // Query personalizada para contar propriedades de um usuário
    @Query("SELECT COUNT(p) FROM Propriedade p WHERE p.usuario.id = :usuarioId")
    long countPropriedadesByUsuarioId(@Param("usuarioId") Long usuarioId);

    Optional<Usuario> findByCognitoId(String cognitoId);
}