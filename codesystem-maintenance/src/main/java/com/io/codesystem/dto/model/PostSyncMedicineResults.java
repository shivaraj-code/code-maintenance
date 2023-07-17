package com.io.codesystem.dto.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import lombok.Data;

@Entity
@Table(name = "medicines")
@Data
public class PostSyncMedicineResults {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@Column(name = "ai_id")
	private int aiid;

	@Column(name = "id")
	private int id;

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

	@Column(name = "data_source")
	private String dataSource;

	@Column(name = "version_state")
	private String versionState;

	@Column(name = "status")
	private String status;

	@Column(name = "is_controlled_substance")
	private Boolean isControlledSubstance;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "modified_by")
	private Integer modifiedBy;

	@Column(name = "modified_date")
	private Date modifiedDate;

	@Column(name = "ref_id")
	private Integer refId;

	@Column(name = "original_ref_id")
	private Integer originalRefId;

	@Column(name = "file_id")
	private Integer fileId;

	@Column(name = "sync_status")
	private String syncStatus;

}
