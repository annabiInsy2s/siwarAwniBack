package com.aweni.aweniBack.repository;

import com.aweni.aweniBack.model.Offre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IOffreRepository extends JpaRepository<Offre, Long > {
}
