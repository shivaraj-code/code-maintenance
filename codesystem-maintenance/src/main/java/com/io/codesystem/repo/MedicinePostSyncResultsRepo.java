package com.io.codesystem.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.io.codesystem.dto.model.PostSyncIcdResults;
import com.io.codesystem.dto.model.PostSyncMedicineResults;

@Repository
public interface MedicinePostSyncResultsRepo  extends JpaRepository<PostSyncMedicineResults, Integer> {

	@Query(value="CALL GetMedicinePostSyncData(:file_id,:status)",nativeQuery=true)
	public List<PostSyncMedicineResults> medicinePostSyncDataResults(Integer file_id,String status);
	
}