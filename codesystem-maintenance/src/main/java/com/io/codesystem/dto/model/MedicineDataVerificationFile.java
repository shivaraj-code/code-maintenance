package com.io.codesystem.dto.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

	@Entity
	@Data
	@Table(name="medicine_data_verification")
	public class MedicineDataVerificationFile {
			@Id
			@GeneratedValue(strategy = GenerationType.IDENTITY)
			@Column(name = "aiid")
			private int aiid;
			
			@Column(name="id")
			public int id;
			
			@Column(name = "ndc")
			private String ndc;

			@Column(name = "name")
			private String name;

			@Column(name = "dea")
			private int dea;

			@Column(name = "obsdtec")
			private String obsdtec;

			@Column(name = "repack")
			private int repack;

			@Column(name = "is_compounded")
			private String isCompounded;
			
			@Column(name="file_id")
		   	public Integer fileId;
		    
			@Column(name="inserted_date")
		   	public Timestamp insertedDate;
			
			@Column(name="status")
		   	public String status;
						
		   
}

