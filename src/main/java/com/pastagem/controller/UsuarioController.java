package com.pastagem.controller;

import com.pastagem.model.Usuario;
import com.pastagem.model.Propriedade;
import com.pastagem.model.Cargo;
import com.pastagem.service.UsuarioService;
import com.pastagem.service.CognitoService;
import com.pastagem.dto.UsuarioRegisterDTO;
import com.pastagem.dto.ErrorResponse;
import com.pastagem.dto.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CognitoService cognitoService;

    // Operações básicas CRUD
    
    @GetMapping
    public ResponseEntity<Page<Usuario>> findAll(@PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<Usuario> usuarios = usuarioService.findAll(pageable);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/todos")
    public ResponseEntity<List<Usuario>> findAll() {
        try {
            List<Usuario> usuarios = usuarioService.findAll();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> findById(@PathVariable Long id) {
        try {
            Optional<Usuario> usuario = usuarioService.findById(id);
            return usuario.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Usuario> save(@Valid @RequestBody Usuario usuario) {
        try {
            Usuario usuarioSalvo = usuarioService.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioSalvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        try {
            if (!usuarioService.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            // Buscar usuário existente para obter o email atual e cognitoId
            Optional<Usuario> usuarioExistente = usuarioService.findById(id);
            if (usuarioExistente.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            String emailAntigo = usuarioExistente.get().getEmail();
            String emailNovo = usuario.getEmail();
            String cognitoId = usuarioExistente.get().getCognitoId();
            
            // Se o email foi alterado, atualizar no Cognito
            if (!emailAntigo.equals(emailNovo)) {
                try {
                    cognitoService.updateUserEmailInCognito(emailAntigo, emailNovo, usuario.getNome());
                } catch (Exception e) {
                    logger.error("Erro ao atualizar email no Cognito: {}", e.getMessage());
                    return ResponseEntity.badRequest().build();
                }
            }
            
            usuario.setId(id);
            usuario.setCognitoId(cognitoId); // Mantém o cognitoId original
            Usuario usuarioAtualizado = usuarioService.save(usuario);
            return ResponseEntity.ok(usuarioAtualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        try {
            usuarioService.deleteById(id);
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

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Usuario> findByCpf(@PathVariable String cpf) {
        try {
            Optional<Usuario> usuario = usuarioService.findByCpf(cpf);
            return usuario.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> findByEmail(@PathVariable String email) {
        try {
            Optional<Usuario> usuario = usuarioService.findByEmail(email);
            return usuario.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Usuario>> findByNomeContaining(@RequestParam String nome) {
        try {
            List<Usuario> usuarios = usuarioService.findByNomeContaining(nome);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/cargo/{cargo}")
    public ResponseEntity<List<Usuario>> findByCargo(@PathVariable Cargo cargo) {
        try {
            List<Usuario> usuarios = usuarioService.findByCargo(cargo);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Verificações de existência

    @GetMapping("/{id}/existe")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        try {
            boolean existe = usuarioService.existsById(id);
            return ResponseEntity.ok(existe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/cpf/{cpf}/existe")
    public ResponseEntity<Boolean> existsByCpf(@PathVariable String cpf) {
        try {
            boolean existe = usuarioService.existsByCpf(cpf);
            return ResponseEntity.ok(existe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/email/{email}/existe")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        try {
            boolean existe = usuarioService.existsByEmail(email);
            return ResponseEntity.ok(existe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Operações relacionadas

    @GetMapping("/{id}/propriedades")
    public ResponseEntity<List<Propriedade>> findPropriedadesByUsuarioId(@PathVariable Long id) {
        try {
            List<Propriedade> propriedades = usuarioService.findPropriedadesByUsuarioId(id);
            return ResponseEntity.ok(propriedades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/propriedades/count")
    public ResponseEntity<Long> countPropriedadesByUsuarioId(@PathVariable Long id) {
        try {
            long count = usuarioService.countPropriedadesByUsuarioId(id);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isValidCargo(String cargo) {
        try {
            Cargo.valueOf(cargo);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String removerPontuacao(String texto) {
        if (texto == null) return null;
        return texto.replaceAll("[^0-9]", "");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioRegisterDTO usuarioDTO) {
        try {
            logger.info("Iniciando registro de usuário: {}", usuarioDTO.getEmail());

            // Validar cargo
            if (usuarioDTO.getCargo() == null || !isValidCargo(usuarioDTO.getCargo())) {
                logger.error("Cargo inválido: {}", usuarioDTO.getCargo());
                return ResponseEntity.badRequest().body(new ErrorResponse("Cargo inválido. Deve ser ADMIN ou PRODUTOR"));
            }

            // Remover pontuação do CPF e telefone
            String cpfLimpo = removerPontuacao(usuarioDTO.getCpf());
            String telefoneLimpo = removerPontuacao(usuarioDTO.getTelefone());

            // Verificar se o usuário já existe no Cognito
            try {
                if (cognitoService.userExistsInCognito(usuarioDTO.getEmail())) {
                    logger.error("Usuário já existe no Cognito: {}", usuarioDTO.getEmail());
                    return ResponseEntity.badRequest().body(new ErrorResponse("Usuário já existe no Cognito"));
                }
            } catch (Exception e) {
                logger.error("Erro ao verificar usuário no Cognito: {}", e.getMessage());
                return ResponseEntity.badRequest().body(new ErrorResponse("Erro ao verificar usuário no Cognito: " + e.getMessage()));
            }

            // Registrar no Cognito
            String cognitoId = cognitoService.registerUserInCognito(
                usuarioDTO.getEmail(),
                usuarioDTO.getPassword(),
                usuarioDTO.getNome()
            );

            // Criar usuário no banco de dados
            Usuario usuario = new Usuario();
            usuario.setNome(usuarioDTO.getNome());
            usuario.setCpf(cpfLimpo);
            usuario.setTelefone(telefoneLimpo);
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setCargo(Cargo.valueOf(usuarioDTO.getCargo()));
            usuario.setCognitoId(cognitoId);

            try {
                usuarioService.save(usuario);
                logger.info("Usuário registrado com sucesso: {}", usuarioDTO.getEmail());
                return ResponseEntity.ok(new MessageResponse("Usuário registrado com sucesso"));
            } catch (Exception e) {
                logger.error("Erro ao salvar usuário no banco de dados: {}", e.getMessage());
                // Tentar remover o usuário do Cognito em caso de erro
                try {
                    cognitoService.deleteUserFromCognito(usuarioDTO.getEmail());
                } catch (Exception ex) {
                    logger.error("Erro ao remover usuário do Cognito após falha no banco: {}", ex.getMessage());
                }
                return ResponseEntity.badRequest().body(new ErrorResponse("Erro ao salvar usuário: " + e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("Erro ao registrar usuário: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Erro ao registrar usuário: " + e.getMessage()));
        }
    }

    @PostMapping("/alterar-senha")
    public ResponseEntity<?> alterarSenha(@RequestBody Map<String, String> request) {
        try {
            String senhaAtual = request.get("senhaAtual");
            String novaSenha = request.get("novaSenha");
            
            if (senhaAtual == null || novaSenha == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Senha atual e nova senha são obrigatórias"));
            }

            cognitoService.alterarSenha(senhaAtual, novaSenha);
            return ResponseEntity.ok(new MessageResponse("Senha alterada com sucesso"));
        } catch (Exception e) {
            logger.error("Erro ao alterar senha: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}