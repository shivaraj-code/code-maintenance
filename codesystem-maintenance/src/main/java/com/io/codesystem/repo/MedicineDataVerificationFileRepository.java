package com.io.codesystem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.io.codesystem.dto.model.Icd10DataVerificationFile;
import com.io.codesystem.dto.model.MedicineDataVerificationFile;

@Repository
public interface MedicineDataVerificationFileRepository  extends JpaRepository<MedicineDataVerificationFile, Integer>{

}

