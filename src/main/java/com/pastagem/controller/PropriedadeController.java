package com.pastagem.controller;

import com.pastagem.model.Propriedade;
import com.pastagem.model.Pastagem;
import com.pastagem.model.Usuario;
import com.pastagem.service.PropriedadeService;
import com.pastagem.service.UsuarioService;
import com.pastagem.dto.PropriedadeCreateDTO;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/propriedade")
@CrossOrigin(origins = "*")
public class PropriedadeController {

    @Autowired
    private PropriedadeService propriedadeService;

    @Autowired
    private UsuarioService usuarioService;

    // Operações básicas CRUD

    @GetMapping("/usuario/{id_usuario}")
    public ResponseEntity<Page<Propriedade>> findAll(@PathVariable Long id_usuario, @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<Propriedade> propriedades = propriedadeService.findByUsuarioId(id_usuario, pageable);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/todas")
    public ResponseEntity<List<Propriedade>> findAll() {
        try {
            List<Propriedade> propriedades = propriedadeService.findAll();
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/usuario/{id_usuario}")
    public ResponseEntity<Propriedade> findById(@PathVariable Long id, @PathVariable Long id_usuario) {
        try {
            Optional<Propriedade> propriedade = propriedadeService.findByIdAndUsuarioId(id, id_usuario);
            return propriedade.map(ResponseEntity::ok)
                             .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/usuario/{id_usuario}")
    public ResponseEntity<Propriedade> save(@PathVariable Long id_usuario, @Valid @RequestBody PropriedadeCreateDTO dto, HttpServletRequest request) {
        try {
            // Pega o ID do usuário do token JWT
            DecodedJWT jwt = (DecodedJWT) request.getAttribute("cognitoUser");
            String userId = jwt.getSubject();

            // Busca o usuário pelo ID do Cognito
            Usuario usuario = usuarioService.findByCognitoId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            // Verifica se o usuário tem permissão
            if (!usuario.getId().equals(id_usuario)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Converte o DTO para entidade
            Propriedade propriedade = new Propriedade();
            propriedade.setNome(dto.getNome());
            propriedade.setEndereco(dto.getEndereco());
            propriedade.setAreaTotal(dto.getAreaTotal());
            propriedade.setCidade(dto.getCidade());
            propriedade.setEstado(dto.getEstado());
            propriedade.setUsuario(usuario);

            // Salva a propriedade
            Propriedade propriedadeSalva = propriedadeService.save(propriedade, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(propriedadeSalva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/usuario/{id_usuario}")
    public ResponseEntity<Propriedade> update(@PathVariable Long id, @PathVariable Long id_usuario, @Valid @RequestBody PropriedadeCreateDTO dto, HttpServletRequest request) {
        try {
            // Busca a propriedade existente
            Propriedade propriedadeExistente = propriedadeService.findByIdAndUsuarioId(id, id_usuario)
                .orElseThrow(() -> new IllegalArgumentException("Propriedade não encontrada"));
            
            // Pega o ID do usuário do token JWT
            DecodedJWT jwt = (DecodedJWT) request.getAttribute("cognitoUser");
            String userId = jwt.getSubject();

            // Verifica se o usuário tem permissão
            if (!propriedadeExistente.getUsuario().getId().equals(id_usuario)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Atualiza apenas os campos necessários
            propriedadeExistente.setNome(dto.getNome());
            propriedadeExistente.setEndereco(dto.getEndereco());
            propriedadeExistente.setAreaTotal(dto.getAreaTotal());
            propriedadeExistente.setCidade(dto.getCidade());
            propriedadeExistente.setEstado(dto.getEstado());
            
            Propriedade propriedadeAtualizada = propriedadeService.save(propriedadeExistente, userId);
            return ResponseEntity.ok(propriedadeAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}/usuario/{id_usuario}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id, @PathVariable Long id_usuario) {
        try {
            // Verifica se a propriedade pertence ao usuário
            Optional<Propriedade> propriedade = propriedadeService.findByIdAndUsuarioId(id, id_usuario);
            if (propriedade.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            propriedadeService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Operações específicas de busca

    @GetMapping("/usuario/{id_usuario}/cidade/{cidade}")
    public ResponseEntity<List<Propriedade>> findByCidade(@PathVariable Long id_usuario, @PathVariable String cidade) {
        try {
            List<Propriedade> propriedades = propriedadeService.findByUsuarioIdAndCidade(id_usuario, cidade);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{id_usuario}/estado/{estado}")
    public ResponseEntity<List<Propriedade>> findByEstado(@PathVariable Long id_usuario, @PathVariable String estado) {
        try {
            List<Propriedade> propriedades = propriedadeService.findByUsuarioIdAndEstado(id_usuario, estado);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{id_usuario}/buscar")
    public ResponseEntity<List<Propriedade>> findByNomeContaining(@PathVariable Long id_usuario, @RequestParam String nome) {
        try {
            List<Propriedade> propriedades = propriedadeService.findByUsuarioIdAndNomeContaining(id_usuario, nome);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{id_usuario}/localidade")
    public ResponseEntity<List<Propriedade>> findByCidadeAndEstado(
            @PathVariable Long id_usuario,
            @RequestParam String cidade, 
            @RequestParam String estado) {
        try {
            List<Propriedade> propriedades = propriedadeService.findByUsuarioIdAndCidadeAndEstado(id_usuario, cidade, estado);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Verificação de existência

    @GetMapping("/{id}/usuario/{id_usuario}/existe")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id, @PathVariable Long id_usuario) {
        try {
            boolean existe = propriedadeService.existsByIdAndUsuarioId(id, id_usuario);
            return ResponseEntity.ok(existe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Operações relacionadas

    @GetMapping("/{id}/usuario/{id_usuario}/pastagens")
    public ResponseEntity<List<Pastagem>> findPastagensByPropriedadeId(@PathVariable Long id, @PathVariable Long id_usuario) {
        try {
            // Verifica se a propriedade pertence ao usuário
            if (!propriedadeService.existsByIdAndUsuarioId(id, id_usuario)) {
                return ResponseEntity.notFound().build();
            }

            List<Pastagem> pastagens = propriedadeService.findPastagensByPropriedadeId(id);
            return ResponseEntity.ok(pastagens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/usuario/{id_usuario}/pastagens/count")
    public ResponseEntity<Long> countPastagensByPropriedadeId(@PathVariable Long id, @PathVariable Long id_usuario) {
        try {
            // Verifica se a propriedade pertence ao usuário
            if (!propriedadeService.existsByIdAndUsuarioId(id, id_usuario)) {
                return ResponseEntity.notFound().build();
            }

            long count = propriedadeService.countPastagensByPropriedadeId(id);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/usuario/{id_usuario}/area-total")
    public ResponseEntity<BigDecimal> calcularAreaTotalPastagens(@PathVariable Long id, @PathVariable Long id_usuario) {
        try {
            // Verifica se a propriedade pertence ao usuário
            if (!propriedadeService.existsByIdAndUsuarioId(id, id_usuario)) {
                return ResponseEntity.notFound().build();
            }

            BigDecimal areaTotal = propriedadeService.calcularAreaTotalPastagens(id);
            return ResponseEntity.ok(areaTotal);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}