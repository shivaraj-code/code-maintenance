package com.io.codesystem.dto.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

	@Entity
	@Table(name = "medicine_sync_data_result")
    @Data
	public class MedicineSyncDataRecords {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "id")
		public int id;
	    
	    @Column(name = "file_id")
		public Integer fileId;
	    
	    @Column(name = "added_records")
		public Integer addedRecords;
	    
	    @Column(name = "updated_records")
		public Integer updatedRecords;
	    
	    @Column(name = "deleted_records")
		public Integer deletedRecords;
	    
	    @Column(name = "status")
		public String status;
	    
		
	}

