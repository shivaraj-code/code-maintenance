package com.io.codesystem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.io.codesystem.dto.model.MedicineSyncResults;

@Repository
public interface MedicineSyncResultsRepo  extends JpaRepository<MedicineSyncResults, Integer> {

	
	@Query(value="CALL MedicinesCompareAndSyncTables(:file_id,:file_name,:user_id)",nativeQuery=true)
	public MedicineSyncResults medicineCompareAndSyncTables(Integer file_id, String file_name, Integer user_id);
	
}
