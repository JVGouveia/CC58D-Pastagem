package com.pastagem.service.impl;

import com.pastagem.model.Propriedade;
import com.pastagem.repository.PropriedadeRepository;
import com.pastagem.service.PropriedadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropriedadeServiceImpl implements PropriedadeService {

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Override
    public List<Propriedade> findAll() {
        return propriedadeRepository.findAll();
    }

    @Override
    public Optional<Propriedade> findById(Long id) {
        return propriedadeRepository.findById(id);
    }

    @Override
    public Propriedade save(Propriedade propriedade) {
        return propriedadeRepository.save(propriedade);
    }

    @Override
    public void deleteById(Long id) {
        propriedadeRepository.deleteById(id);
    }
} 