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

    @GetMapping
    public ResponseEntity<Page<Propriedade>> findAll(@PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<Propriedade> propriedades = propriedadeService.findAll(pageable);
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

    @GetMapping("/{id}")
    public ResponseEntity<Propriedade> findById(@PathVariable Long id) {
        try {
            Optional<Propriedade> propriedade = propriedadeService.findById(id);
            return propriedade.map(ResponseEntity::ok)
                             .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Propriedade> save(@Valid @RequestBody PropriedadeCreateDTO dto, HttpServletRequest request) {
        try {
            // Pega o ID do usuário do token JWT
            DecodedJWT jwt = (DecodedJWT) request.getAttribute("cognitoUser");
            String userId = jwt.getSubject();

            // Busca o usuário pelo ID do Cognito
            Usuario usuario = usuarioService.findByCognitoId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

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

    @PutMapping("/{id}")
    public ResponseEntity<Propriedade> update(@PathVariable Long id, @Valid @RequestBody PropriedadeCreateDTO dto, HttpServletRequest request) {
        try {
            // Busca a propriedade existente
            Propriedade propriedadeExistente = propriedadeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Propriedade não encontrada"));
            
            // Pega o ID do usuário do token JWT
            DecodedJWT jwt = (DecodedJWT) request.getAttribute("cognitoUser");
            String userId = jwt.getSubject();

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        try {
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

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Propriedade>> findByUsuarioId(@PathVariable Long usuarioId) {
        try {
            List<Propriedade> propriedades = propriedadeService.findByUsuarioId(usuarioId);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/cidade/{cidade}")
    public ResponseEntity<List<Propriedade>> findByCidade(@PathVariable String cidade) {
        try {
            List<Propriedade> propriedades = propriedadeService.findByCidade(cidade);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Propriedade>> findByEstado(@PathVariable String estado) {
        try {
            List<Propriedade> propriedades = propriedadeService.findByEstado(estado);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Propriedade>> findByNomeContaining(@RequestParam String nome) {
        try {
            List<Propriedade> propriedades = propriedadeService.findByNomeContaining(nome);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/localidade")
    public ResponseEntity<List<Propriedade>> findByCidadeAndEstado(
            @RequestParam String cidade, 
            @RequestParam String estado) {
        try {
            List<Propriedade> propriedades = propriedadeService.findByCidadeAndEstado(cidade, estado);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Verificação de existência

    @GetMapping("/{id}/existe")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        try {
            boolean existe = propriedadeService.existsById(id);
            return ResponseEntity.ok(existe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Operações relacionadas

    @GetMapping("/{id}/pastagens")
    public ResponseEntity<List<Pastagem>> findPastagensByPropriedadeId(@PathVariable Long id) {
        try {
            List<Pastagem> pastagens = propriedadeService.findPastagensByPropriedadeId(id);
            return ResponseEntity.ok(pastagens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/pastagens/count")
    public ResponseEntity<Long> countPastagensByPropriedadeId(@PathVariable Long id) {
        try {
            long count = propriedadeService.countPastagensByPropriedadeId(id);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/area-total")
    public ResponseEntity<BigDecimal> calcularAreaTotalPastagens(@PathVariable Long id) {
        try {
            BigDecimal areaTotal = propriedadeService.calcularAreaTotalPastagens(id);
            return ResponseEntity.ok(areaTotal);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}