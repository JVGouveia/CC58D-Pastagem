package com.pastagem.controller;

import com.pastagem.model.Produtor;
import com.pastagem.service.ProdutorService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtores")
public class ProdutorController {

    @Autowired
    private ProdutorService produtorService;

    @GetMapping
    public ResponseEntity<List<Produtor>> getAllProdutores() {
        return ResponseEntity.ok(produtorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produtor> getProdutorById(@PathVariable Long id) {
        return produtorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Produtor> createProdutor(@Valid @RequestBody Produtor produtor) {
        return ResponseEntity.ok(produtorService.save(produtor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produtor> updateProdutor(@PathVariable Long id,@Valid @RequestBody Produtor produtor) {
        return produtorService.findById(id)
                .map(existingProdutor -> {
                    produtor.setId(id);
                    return ResponseEntity.ok(produtorService.save(produtor));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProdutor(@PathVariable Long id) {
        return produtorService.findById(id)
                .map(produtor -> {
                    produtorService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 