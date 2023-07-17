package com.io.codesystem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.io.codesystem.dto.model.MedicineSyncDataRecords;

	@Repository
	public interface MedicineSyncDataRecordsRepo  extends JpaRepository<MedicineSyncDataRecords, Integer> {

	public MedicineSyncDataRecords findByStatus(String status);
}
