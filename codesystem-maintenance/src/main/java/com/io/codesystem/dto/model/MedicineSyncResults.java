package com.io.codesystem.dto.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class MedicineSyncResults {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	private int added_records;
	private int updated_records;
	private int deleted_records;
	private String status;

}
@Data
class MedicineSyncDataResult{
	
	@Id
	public Integer id;
	private int file_id;
	private int added_records;
	private int updated_records;
	private int deleted_records;
	private String status;

}