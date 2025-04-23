package com.pastagem.service;

import com.pastagem.model.Propriedade;
import java.util.List;
import java.util.Optional;

public interface PropriedadeService {
    List<Propriedade> findAll();
    Optional<Propriedade> findById(Long id);
    Propriedade save(Propriedade propriedade);
    void deleteById(Long id);
} 