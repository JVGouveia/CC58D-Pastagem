package com.pastagem.controller;

import com.pastagem.model.AreaPastagem;
import com.pastagem.service.AreaPastagemService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/areas-pastagem")
public class AreaPastagemController {

    @Autowired
    private AreaPastagemService areaPastagemService;

    @GetMapping
    public ResponseEntity<List<AreaPastagem>> getAllAreasPastagem() {
        return ResponseEntity.ok(areaPastagemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaPastagem> getAreaPastagemById(@PathVariable Long id) {
        return areaPastagemService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AreaPastagem> createAreaPastagem(@Valid @RequestBody AreaPastagem areaPastagem) {
        return ResponseEntity.ok(areaPastagemService.save(areaPastagem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaPastagem> updateAreaPastagem(@PathVariable Long id, @Valid @RequestBody AreaPastagem areaPastagem) {
        return areaPastagemService.findById(id)
                .map(existingAreaPastagem -> {
                    areaPastagem.setId(id);
                    return ResponseEntity.ok(areaPastagemService.save(areaPastagem));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAreaPastagem(@PathVariable Long id) {
        return areaPastagemService.findById(id)
                .map(areaPastagem -> {
                    areaPastagemService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 