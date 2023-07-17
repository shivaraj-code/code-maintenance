package com.io.codesystem.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.io.codesystem.dto.model.MedicineDataVerificationFile;

@Repository
public interface MedicineDataVerificationFileRepository extends JpaRepository<MedicineDataVerificationFile, Integer> {

	 @Query(value="CALL GetMedicineVerificationDetails(:fileId,:ndc,:name,:status)",nativeQuery=true)
	 public List<MedicineDataVerificationFile> getMedicinesVerificationDetails(int fileId, String ndc, String name,String status);
	
}
