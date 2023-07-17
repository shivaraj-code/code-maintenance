package com.io.codesystem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.io.codesystem.dto.model.Icd10SyncDataRecords;

	@Repository
	public interface Icd10SyncDataRecordsRepo  extends JpaRepository<Icd10SyncDataRecords, Integer> {

	public Icd10SyncDataRecords findByStatus(String status);
}
