package com.pastagem.controller;

import com.pastagem.model.Pastagem;
import com.pastagem.service.PastagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pastagem")
@CrossOrigin(origins = "*")
public class PastagemController {

    @Autowired
    private PastagemService pastagemService;

    // Operações básicas CRUD

    @GetMapping
    public ResponseEntity<Page<Pastagem>> findAll(@PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<Pastagem> pastagens = pastagemService.findAll(pageable);
            return ResponseEntity.ok(pastagens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/todas")
    public ResponseEntity<List<Pastagem>> findAll() {
        try {
            List<Pastagem> pastagens = pastagemService.findAll();
            return ResponseEntity.ok(pastagens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pastagem> findById(@PathVariable Long id) {
        try {
            Optional<Pastagem> pastagem = pastagemService.findById(id);
            return pastagem.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Pastagem> save(@Valid @RequestBody Pastagem pastagem) {
        try {
            Pastagem pastagemSalva = pastagemService.save(pastagem);
            return ResponseEntity.status(HttpStatus.CREATED).body(pastagemSalva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Pastagem pastagem) {
        try {
            if (!pastagemService.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            pastagem.setId(id);
            Pastagem pastagemAtualizada = pastagemService.save(pastagem);
            return ResponseEntity.ok(pastagemAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        try {
            pastagemService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Operações específicas de busca

    @GetMapping("/propriedade/{propriedadeId}")
    public ResponseEntity<List<Pastagem>> findByPropriedadeId(@PathVariable Long propriedadeId) {
        try {
            List<Pastagem> pastagens = pastagemService.findByPropriedadeId(propriedadeId);
            return ResponseEntity.ok(pastagens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tipo-pasto/{tipoPasto}")
    public ResponseEntity<List<Pastagem>> findByTipoPasto(@PathVariable String tipoPasto) {
        try {
            List<Pastagem> pastagens = pastagemService.findByTipoPasto(tipoPasto);
            return ResponseEntity.ok(pastagens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Pastagem>> findByNomeContaining(@RequestParam String nome) {
        try {
            List<Pastagem> pastagens = pastagemService.findByNomeContaining(nome);
            return ResponseEntity.ok(pastagens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/area-range")
    public ResponseEntity<List<Pastagem>> findByAreaHectaresBetween(
            @RequestParam BigDecimal areaMin, 
            @RequestParam BigDecimal areaMax) {
        try {
            List<Pastagem> pastagens = pastagemService.findByAreaHectaresBetween(areaMin, areaMax);
            return ResponseEntity.ok(pastagens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/capacidade-range")
    public ResponseEntity<List<Pastagem>> findByCapacidadeSuporteBetween(
            @RequestParam Integer capacidadeMin, 
            @RequestParam Integer capacidadeMax) {
        try {
            List<Pastagem> pastagens = pastagemService.findByCapacidadeSuporteBetween(capacidadeMin, capacidadeMax);
            return ResponseEntity.ok(pastagens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Verificação de existência

    @GetMapping("/{id}/existe")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        try {
            boolean existe = pastagemService.existsById(id);
            return ResponseEntity.ok(existe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Operações de relatório e análise

    @GetMapping("/capacidade-inadequada")
    public ResponseEntity<List<Pastagem>> findPastagensComCapacidadeInadequada() {
        try {
            List<Pastagem> pastagens = pastagemService.findPastagensComCapacidadeInadequada();
            return ResponseEntity.ok(pastagens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/propriedade/{propriedadeId}/area-total")
    public ResponseEntity<BigDecimal> calcularAreaTotalPorPropriedade(@PathVariable Long propriedadeId) {
        try {
            BigDecimal areaTotal = pastagemService.calcularAreaTotalPorPropriedade(propriedadeId);
            return ResponseEntity.ok(areaTotal);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/propriedade/{propriedadeId}/capacidade-total")
    public ResponseEntity<Integer> calcularCapacidadeTotalPorPropriedade(@PathVariable Long propriedadeId) {
        try {
            Integer capacidadeTotal = pastagemService.calcularCapacidadeTotalPorPropriedade(propriedadeId);
            return ResponseEntity.ok(capacidadeTotal);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/propriedade/{propriedadeId}/densidade-media")
    public ResponseEntity<BigDecimal> calcularDensidadeMediaPorPropriedade(@PathVariable Long propriedadeId) {
        try {
            BigDecimal densidadeMedia = pastagemService.calcularDensidadeMediaPorPropriedade(propriedadeId);
            return ResponseEntity.ok(densidadeMedia);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/relatorio/resumo-propriedades")
    public ResponseEntity<List<Object[]>> relatorioResumoPropriedades() {
        try {
            List<Object[]> relatorio = pastagemService.relatorioResumoPropriedades();
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/relatorio/por-tipo-pasto")
    public ResponseEntity<List<Object[]>> relatorioPorTipoPasto() {
        try {
            List<Object[]> relatorio = pastagemService.relatorioPorTipoPasto();
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}