package com.io.codesystem.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.io.codesystem.dto.model.IcdSyncResults;

@Repository
public interface IcdSyncResultsRepo  extends JpaRepository<IcdSyncResults, Integer> {

	@Query(value="CALL IcdCompareAndSyncTables(:file_id,:file_name,:user_id)",nativeQuery=true)
		public IcdSyncResults icdCompareAndSyncTables(Integer file_id,String file_name,Integer user_id);
	
}
