package com.pastagem.controller;

import com.pastagem.model.Propriedade;
import com.pastagem.service.PropriedadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/propriedades")
public class PropriedadeController {

    @Autowired
    private PropriedadeService propriedadeService;

    @GetMapping
    public ResponseEntity<List<Propriedade>> getAllPropriedades() {
        return ResponseEntity.ok(propriedadeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Propriedade> getPropriedadeById(@PathVariable Long id) {
        return propriedadeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Propriedade> createPropriedade(@RequestBody Propriedade propriedade) {
        return ResponseEntity.ok(propriedadeService.save(propriedade));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Propriedade> updatePropriedade(@PathVariable Long id, @RequestBody Propriedade propriedade) {
        return propriedadeService.findById(id)
                .map(existingPropriedade -> {
                    propriedade.setId(id);
                    return ResponseEntity.ok(propriedadeService.save(propriedade));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePropriedade(@PathVariable Long id) {
        return propriedadeService.findById(id)
                .map(propriedade -> {
                    propriedadeService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 