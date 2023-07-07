package com.io.codesystem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.io.codesystem.dto.model.Icd10DataVerificationFile;

@Repository
public interface Icd10DataVerificationFileRepository  extends JpaRepository<Icd10DataVerificationFile, Integer>{

}

