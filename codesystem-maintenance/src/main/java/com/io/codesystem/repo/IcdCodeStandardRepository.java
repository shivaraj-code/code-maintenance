package com.io.codesystem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.io.codesystem.dto.model.IcdCodeStandard;

public interface IcdCodeStandardRepository extends JpaRepository<IcdCodeStandard, Integer> {
	
	@Query(value="CALL PrepareIcdDataForVerification(:file_id,:file_name,:user_id)",nativeQuery=true)
	public void prepareIcdDataForVerification(Integer file_id,String file_name,Integer user_id);
	
	

}
