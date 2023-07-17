package com.io.codesystem.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.io.codesystem.dto.model.Icd10DataVerificationFile;


@Repository
public interface Icd10DataVerificationFileRepository  extends JpaRepository<Icd10DataVerificationFile, Integer>{
	
	
	@Query(value="CALL GetIcdCodeVerificationDetails(:fileid,:icdcode,:codedesc,:status)",nativeQuery=true)
	public List<Icd10DataVerificationFile> getIcdCodeVerificationDetails(Integer fileid,String icdcode,String codedesc,String status);
			
	
}

