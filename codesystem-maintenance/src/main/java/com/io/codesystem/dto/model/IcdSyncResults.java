package com.io.codesystem.dto.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class IcdSyncResults {
	
	@Id
	public Integer id;
	private int added_records;
	private int updated_records;
	private int deleted_records;
	private String status;

}
