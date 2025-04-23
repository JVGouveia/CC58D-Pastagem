package com.pastagem.service.impl;

import com.pastagem.model.AreaPastagem;
import com.pastagem.repository.AreaPastagemRepository;
import com.pastagem.service.AreaPastagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AreaPastagemServiceImpl implements AreaPastagemService {

    @Autowired
    private AreaPastagemRepository areaPastagemRepository;

    @Override
    public List<AreaPastagem> findAll() {
        return areaPastagemRepository.findAll();
    }

    @Override
    public Optional<AreaPastagem> findById(Long id) {
        return areaPastagemRepository.findById(id);
    }

    @Override
    public AreaPastagem save(AreaPastagem areaPastagem) {
        return areaPastagemRepository.save(areaPastagem);
    }

    @Override
    public void deleteById(Long id) {
        areaPastagemRepository.deleteById(id);
    }
} 