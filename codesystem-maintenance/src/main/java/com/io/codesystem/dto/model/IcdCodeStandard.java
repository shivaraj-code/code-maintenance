package com.io.codesystem.dto.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

	@Entity
	@Data
	@Table(name="icd_standard_versions")
	public class IcdCodeStandard {
			@Id
			@GeneratedValue(strategy = GenerationType.IDENTITY)
			@Column(name="id")
			public int id;
			@Column(name="icd_order")
			public String icdOrder; 
		    @Column(name="icd_code")
			public String icdCode;  
		    @Column(name="icd_id")
			public Integer icdId;  
		    @Column(name="type")
		   	public Character type;  
		    @Column(name="short_desc")
		   	public String shortDesc;  
		    @Column(name="medium_desc")
		   	public String medDesc;  
		    @Column(name="long_desc")
		   	public String longDesc;
		   
}
