package com.io.codesystem.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.io.codesystem.dto.model.CodeStandardFile;
import com.io.codesystem.dto.model.MedicineDataVerificationFile;
import com.io.codesystem.dto.model.MedicineStandard;
import com.io.codesystem.dto.model.MedicineSyncDataRecords;
import com.io.codesystem.dto.model.MedicineSyncResults;
import com.io.codesystem.dto.model.PostSyncMedicineResults;
import com.io.codesystem.repo.CodeStandardFileRepository;
import com.io.codesystem.repo.MedicineDataVerificationFileRepository;
import com.io.codesystem.repo.MedicinePostSyncResultsRepo;
import com.io.codesystem.repo.MedicineStandardRepository;
import com.io.codesystem.repo.MedicineSyncDataRecordsRepo;
import com.io.codesystem.repo.MedicineSyncResultsRepo;
import com.io.codesystem.utility.S3Utility;

@Service
public class MedicineStandardService {

	@Autowired
	private S3Utility s3Utility;

	@Autowired
	private CodeStandardFileRepository codeStandardFileRepository;

	@Autowired
	private MedicineStandardRepository medicineStandardRepository;

	@Autowired
	private MedicineDataVerificationFileRepository medicineDataVerificationFileRepository;

	@Autowired
	private MedicineSyncResultsRepo medicineSyncResultsRepo;

	@Autowired
	private MedicineSyncDataRecordsRepo medicineSyncDataRecordsRepo;

	@Autowired
	private MedicinePostSyncResultsRepo medicinePostSyncResultsRepo;

	@Autowired
	CodeMaintenanceLogService codeMaintenanceLogService;

	@PersistenceContext
	private EntityManager entityManager;

	String dumpTableName = "medicines_secure_latestrecords_07_2023";
	CodeStandardFile metafile = null;
	int id;
	int globalUserId = 0;

	@Transactional
	public String processCodeStandardFileData(CodeStandardFile file, String bucketName, int userId) throws IOException {

//		globalUserId=userId;
		if (s3Utility.isExist(bucketName, file.getFilePath())) {

			try (InputStream inputStream = s3Utility.getS3File(bucketName, file.getFilePath())) {

				metafile = file;
				// String targetFileName = getTargetFilePathFromZipFolder(file.getFileName());
				String targetFileName = "medicines_secure_latestrecords_07_2023.csv";
				fileProcessAndInsertData(inputStream, targetFileName, userId, file);
			}

		}
//		codeMaintenanceLogService.saveCodeMaintenanceLog(file.getId(),
//			    "File Deletion", "File Deleted Successfully", globalUserId);
		return "success";
	}

	@Transactional
	public void fileProcessAndInsertData(InputStream zipInputStream, String targetFileName, int userId,
			CodeStandardFile file) throws IOException {
		File targetFile;
		System.out.println("Zip file processing started");
		// String status = "failed";
		try (ZipInputStream zip = new ZipInputStream(zipInputStream)) {
			ZipEntry entry;
			System.out.println("************" + zip.getNextEntry().getName());
			globalUserId = userId;
			updateTempStatusOfCodeStandardFile("Zip File Extracted");

			String entryName = "medicines_secure_latestrecords_07_2023.csv";
			System.out.println(entryName);
			System.out.println(targetFileName);

			if (entryName.equalsIgnoreCase(targetFileName)) {
				System.out.println("Checking the target and file");

				targetFile = File.createTempFile("temp", ".tmp");

				System.out.println(targetFile.getName());
				try (OutputStream outputStream = new FileOutputStream(targetFile)) {
					byte[] buffer = new byte[1024];
					int length;

					while ((length = zip.read(buffer)) >= 0) {
						outputStream.write(buffer, 0, length);
					}

					codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(), "Zip File Processing",
							"Zip File Processing Successfully", globalUserId);
					updateTempStatusOfCodeStandardFile("Reading of File Content Completed");
					// Process and insert the data
					System.out.println("Dump Table Name:" + dumpTableName);
					insertDataIntoDatabase(targetFile, dumpTableName, userId);
				}

			}

		}
	}

	@Transactional
	public void insertDataIntoDatabase(File file, String newTableName, int userId) {
		globalUserId = userId;
		// List<MedicineStandard> medicineStandardList = new LinkedList<>();
		// IcdSyncResults icdSyncResults=new IcdSyncResults();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
			List<MedicineStandard> medicineStandardList = new LinkedList<>();
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			for (CSVRecord record : csvRecords) {
				MedicineStandard medicines = new MedicineStandard();

				medicines.setId(Integer.parseInt(record.get(0)));
				medicines.setNdc(record.get(1));
				medicines.setName(record.get(2));
				medicines.setDea(Integer.parseInt(record.get(3)));
				medicines.setObsdtec(record.get(4));
				medicines.setRepack(Integer.parseInt(record.get(5)));
				medicines.setIsCompounded(record.get(6));

				medicineStandardList.add(medicines);
			}
			medicineStandardRepository.saveAll(medicineStandardList);

			saveMedicineStandardList(medicineStandardList, newTableName, userId);

			System.out.println("table created");

			codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(),
					"Medicine Data Verification Table Created", "Medicine Data Verification Table Created Successfully",
					globalUserId);

		} catch (IOException e) {
			codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(),
					"Medicine Data Verification Table Creation Failed",
					"Medicine Data Verification Table Creation Failed", globalUserId);

			System.err.println("An error occurred: " + e.getMessage());
			e.printStackTrace();

		}

	}

	@Transactional
	public void saveMedicineStandardList(List<MedicineStandard> medicineStandardList, String newTableName, int userId) {
		try {

			globalUserId = userId;
			// truncateTable("medicines_standard_versions");
			dropTable(newTableName);
			System.out.println("Before Saving");
			medicineStandardRepository.saveAll(medicineStandardList);
			createNewTableFromExisting(newTableName);
			System.out.println("After Saving");
			updateTempStatusOfCodeStandardFile("Dump Table Created");
			codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(), "Dump Table Created",
					"Dump Table Created Successfully", globalUserId);
			System.out.println(
					"After Dump Table Status Updated" + metafile.getId() + ":" + dumpTableName + ":" + globalUserId);
			medicineStandardRepository.prepareMedicineDataForVerification(metafile.getId(), dumpTableName,
					globalUserId);

			System.out.println("Data Prepared for Verification");

			updateTempStatusOfCodeStandardFile("Medicines Verification Data Prepared");
			codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(), "Medicines Verification Data Prepared",
					"Medicines Verification Data Prepared Successfully", globalUserId);

//		truncateTable("icd_standard_versions");
		} catch (Exception e) {
			codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(),
					"Medicines Verification Data Prepared Failed", "Medicines Verification Data Preparation Failed",
					globalUserId);
			System.out.println(e.getMessage());

		}

	}

	@Transactional
	public String dropTable(String tableName) {
		System.out.println("Dropping the dump Table if exists");
		String status = "success";
		try {
			entityManager.createNativeQuery("Drop TABLE IF EXISTS " + tableName).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			status = "failed";
		}
		return status;
	}

	@Transactional
	public String truncateTable(String tableName) {
		System.out.println("Truncating the Version Table");
		String status = "success";
		try {
			entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			status = "failed";
		}
		return status;
	}

	@Transactional
	public String createNewTableFromExisting(String newTableName) {
		System.out.println("New Table Creating from version Table");
		String status = "success";
		try {
			System.out.println("Table have to created with dump name");
			entityManager
					.createNativeQuery("CREATE TABLE " + newTableName + " AS SELECT * FROM medicines_standard_versions")
					.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			status = "failed";
		}
		return status;
	}

	@Transactional
	public void updateTempStatusOfCodeStandardFile(String statusValue) {

		metafile.setCurrentStatus(statusValue);
		Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
		// System.out.println(newModifiedDate.toString());
		metafile.setModifiedDate(newModifiedDate);
		codeStandardFileRepository.save(metafile);
	}

	public String getTargetFilePathFromZipFolder(String zipFileName) {

		// Assuming this is target File Name
		// "icd102022/dot/icd10cm_2022/icd10cm_2022_tab.txt";
		// medicines_secure_latestrecords/medicines_secure_latestrecords.csv
		// String tempName = zipFileName.replace("medicines",
		// "medicines_new_dump").replace(".zip", "");

		String targetFileName = zipFileName.replace(".zip", "") + "/" + "medicines_secure_latestrecords_07_2023.csv";
		// dumpTableName = "medicines_secure_latestrecords";
		return targetFileName;
	}

	@Transactional
	public MedicineSyncResults getMedicineSyncResults(int id,int userId)
	{
		globalUserId = userId;
		MedicineSyncResults medicineSyncResults=new MedicineSyncResults();
		String fileName;
		Integer fileId;
		Optional<CodeStandardFile> file= codeStandardFileRepository.findById(id);
		if (file.isPresent()) {
	    CodeStandardFile codeStandardFile = file.get();
	    fileId = codeStandardFile.getId();
	    fileName = codeStandardFile.getFileName();
	    
	    String tempName = "medicine"; //fileName.replace("pharmacy", "pharmacy").replace(".zip", ""); 
	    String taregtFileName = tempName+"_tab";
	    
	    medicineSyncResults=medicineSyncResultsRepo.medicineCompareAndSyncTables(fileId, taregtFileName, globalUserId);
	    //updateTempStatusOfCodeStandardFile("synching completed");
	    codeMaintenanceLogService.saveCodeMaintenanceLog(codeStandardFile.getId(),
			    "Medicine Data Synching Completed", "Medicine Data Synching Completed Successfully", globalUserId);
	    System.out.println("Before truncateTable method call");
	    truncateTable("medicines_standard_versions");
	    System.out.println("After truncateTable method call");
	    codeStandardFile.setCurrentStatus("synching completed");
	    codeStandardFile.setComments("Medicine File Proceessed Successfully");
	    Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
    	//System.out.println(newModifiedDate.toString());
	    codeStandardFile.setModifiedDate(newModifiedDate);
		codeStandardFileRepository.save(codeStandardFile);
//		int addedRecords=icdSyncResults.getAdded_records();
//		int updatedRecords=icdSyncResults.getUpdated_records();
//		int deletedRecords=icdSyncResults.getDeleted_records();
		//storeRecordLogAfterSync(fileId,addedRecords,deletedRecords,updatedRecords);
	    return medicineSyncResults;
		} else {
	        // Handle case where codeStandardFile is not found
	        return medicineSyncResults;
	    }
	}


	@Transactional
	public MedicineSyncDataRecords getMedicineSyncCounts(int fileId) {

		String syncStatus = "";
		Optional<CodeStandardFile> file = codeStandardFileRepository.findById(fileId);
		if (file.isPresent()) {

			CodeStandardFile metaFile = file.get();
			if (metaFile.getCurrentStatus().equalsIgnoreCase("Medicines Verification Data Prepared")) {
				syncStatus = "Pre Sync";
			}
			if (metaFile.getCurrentStatus().equalsIgnoreCase("Synching Completed")) {
				syncStatus = "Post Sync";
			}

		}
		return medicineSyncDataRecordsRepo.findByStatus(syncStatus);
	}

	/*
	 * public void storeRecordLogAfterSync(Integer fileId, Integer addedRecords,
	 * Integer deletedRecords, Integer updatedRecords) {
	 * 
	 * MedicineSyncDataRecords medicineSyncDataRecords = new
	 * MedicineSyncDataRecords(); medicineSyncDataRecords.setFileId(fileId);
	 * medicineSyncDataRecords.setAddedRecords(addedRecords);
	 * medicineSyncDataRecords.setDeletedRecords(deletedRecords);
	 * medicineSyncDataRecords.setUpdatedRecords(updatedRecords);
	 * medicineSyncDataRecordsRepo.save(medicineSyncDataRecords); }
	 */

	public List<PostSyncMedicineResults> getMedicineSyncResults(int fileId, String status) {
		// TODO Auto-generated method stub
		return medicinePostSyncResultsRepo.medicinePostSyncDataResults(fileId, status);

	}

	/*
	 * public List<MedicineDataVerificationFile> getNameorNDC(int fileId, String
	 * ndc, String name, String status) { // TODO Auto-generated method stub return
	 * medicineDataVerificationFileRepository.getMedicinesVerificationDetails(
	 * fileId, ndc, name, status); }
	 */
	public Page<MedicineDataVerificationFile> getNameorNDC(int fileId, String ndc, String name, String status,int pageSize, int pageNumber) {
		// TODO Auto-generated method stub
		
	        Pageable paging = PageRequest.of(pageNumber, pageSize);

	        List<MedicineDataVerificationFile> medicineVerificationList = medicineDataVerificationFileRepository.getMedicinesVerificationDetails(
	                fileId, ndc, name,status);

	        Page<MedicineDataVerificationFile> pagedResult = new PageImpl<>(
	                medicineVerificationList.subList(
	                        Math.min(pageNumber * pageSize, medicineVerificationList.size()),
	                        Math.min((pageNumber + 1) * pageSize, medicineVerificationList.size())
	                ), paging, medicineVerificationList.size());

	        return pagedResult;
	    }
	
}
