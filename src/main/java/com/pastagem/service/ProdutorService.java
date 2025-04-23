package com.pastagem.service;

import com.pastagem.model.Produtor;
import java.util.List;
import java.util.Optional;

public interface ProdutorService {
    List<Produtor> findAll();
    Optional<Produtor> findById(Long id);
    Produtor save(Produtor produtor);
    void deleteById(Long id);
} 