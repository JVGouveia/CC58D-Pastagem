package com.pastagem.repository;

import com.pastagem.model.AreaPastagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaPastagemRepository extends JpaRepository<AreaPastagem, Long> {
} 