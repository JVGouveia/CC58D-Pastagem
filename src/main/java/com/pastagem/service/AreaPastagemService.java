package com.pastagem.service;

import com.pastagem.model.AreaPastagem;
import java.util.List;
import java.util.Optional;

public interface AreaPastagemService {
    List<AreaPastagem> findAll();
    Optional<AreaPastagem> findById(Long id);
    AreaPastagem save(AreaPastagem areaPastagem);
    void deleteById(Long id);
} 