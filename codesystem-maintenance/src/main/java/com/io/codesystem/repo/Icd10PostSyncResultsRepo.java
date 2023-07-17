package com.io.codesystem.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.io.codesystem.dto.model.PostSyncIcdResults;

@Repository
public interface Icd10PostSyncResultsRepo  extends JpaRepository<PostSyncIcdResults, Integer> {

	@Query(value="CALL GetIcdPostSyncData(:file_id,:status)",nativeQuery=true)
	public List<PostSyncIcdResults> icdPostSyncDataResults(Integer file_id,String status);
	
}