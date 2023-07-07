package com.io.codesystem.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.io.codesystem.dto.model.CodeStandardFile;

@Repository
public interface CodeStandardFileRepository  extends JpaRepository<CodeStandardFile, Integer>{

	@Query(value="select * from coding_standard_file where id= :fileId and processed_status= 'Uploaded' and active=1",nativeQuery=true)
	public Optional<CodeStandardFile> getCodeStandardFileInfo(int fileId);
}
