package com.pastagem.service.impl;

import com.pastagem.model.Produtor;
import com.pastagem.repository.ProdutorRepository;
import com.pastagem.service.ProdutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutorServiceImpl implements ProdutorService {

    @Autowired
    private ProdutorRepository produtorRepository;

    @Override
    public List<Produtor> findAll() {
        return produtorRepository.findAll();
    }

    @Override
    public Optional<Produtor> findById(Long id) {
        return produtorRepository.findById(id);
    }

    @Override
    public Produtor save(Produtor produtor) {
        return produtorRepository.save(produtor);
    }

    @Override
    public void deleteById(Long id) {
        produtorRepository.deleteById(id);
    }
} 