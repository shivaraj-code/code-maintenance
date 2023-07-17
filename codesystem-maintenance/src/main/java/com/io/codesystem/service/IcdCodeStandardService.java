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
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.io.codesystem.dto.model.CodeStandardFile;
import com.io.codesystem.dto.model.Icd10DataVerificationFile;
import com.io.codesystem.dto.model.Icd10SyncDataRecords;
import com.io.codesystem.dto.model.IcdCodeStandard;
import com.io.codesystem.dto.model.IcdSyncResults;
import com.io.codesystem.dto.model.PostSyncIcdResults;
import com.io.codesystem.repo.CodeStandardFileRepository;
import com.io.codesystem.repo.Icd10DataVerificationFileRepository;
import com.io.codesystem.repo.Icd10PostSyncResultsRepo;
import com.io.codesystem.repo.Icd10SyncDataRecordsRepo;
import com.io.codesystem.repo.IcdCodeStandardRepository;
import com.io.codesystem.repo.IcdSyncResultsRepo;
import com.io.codesystem.utility.S3Utility;

@Service
public class IcdCodeStandardService {

	@Autowired
	private S3Utility s3Utility;

	@Autowired
	IcdCodeStandardRepository icdCodeStandardRepository;

	@Autowired
	CodeStandardFileRepository codeStandardFileRepository;

	@Autowired
	Icd10DataVerificationFileRepository icd10DataVerificationFileRepository;
	
	@Autowired
	IcdSyncResultsRepo icdSyncResultsRepo;

	@Autowired
	CodeMaintenanceLogService codeMaintenanceLogService;

	@Autowired
	Icd10SyncDataRecordsRepo icd10SyncDataRecordsRepo;

	@Autowired
	Icd10PostSyncResultsRepo icd10PostSyncResultsRepo;

	@PersistenceContext
	private EntityManager entityManager;

	String dumpTableName = "icd10_dump";
	CodeStandardFile metafile = null;
	int id;
	int globalUserId = 0;

	@Transactional
	public String processCodeStandardFileData(CodeStandardFile file, String bucketName, int userId) throws IOException {

//		globalUserId=userId;
		if (s3Utility.isExist(bucketName, file.getFilePath())) {

			try (InputStream inputStream = s3Utility.getS3File(bucketName, file.getFilePath())) {

				metafile = file;
				String targetFileName = getTargetFilePathFromZipFolder(file.getFileName());
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
		File targetFile = null;
		System.out.println("Zip file processing started");
		// String status = "failed";
		try (ZipInputStream zip = new ZipInputStream(zipInputStream)) {
			ZipEntry entry;
			System.out.println("************" + zip.getNextEntry().getName());
			globalUserId = userId;
			updateTempStatusOfCodeStandardFile("Zip File Extracted");

			while ((entry = zip.getNextEntry()) != null) {
				String entryName = entry.getName();
				System.out.println(entryName);

				if (entryName.equalsIgnoreCase(targetFileName)) {
					System.out.println("Checking the target and file");
					targetFile = File.createTempFile("temp", ".tmp");

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
					break;
				}

			}
		}
	}

	@Transactional
	public void insertDataIntoDatabase(File file, String newTableName, int userId) {
		globalUserId = userId;
		List<IcdCodeStandard> icdCodeStandardList = new LinkedList<>();
		// IcdSyncResults icdSyncResults=new IcdSyncResults();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			System.out.println("file reading");
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				// Process the line and insert the data using Hibernate
				// Example: Splitting line by tab delimiter
				// System.out.println("LINE:" + line);
				String[] data = line.split("\t");

				// Assuming you have an entity class named "EntityClass"
				IcdCodeStandard entity = new IcdCodeStandard();
				entity.setIcdOrder(data[0]);
				entity.setIcdCode(data[1]);
				entity.setIcdId(Integer.parseInt(data[2]));
				entity.setType(data[3].charAt(0));
				entity.setShortDesc(data[4]);
				entity.setMedDesc(data[5]);
				entity.setLongDesc(data[6]);

				icdCodeStandardList.add(entity);
				// System.out.println("entries added to the table");
			}

			saveIcdCodeStandardList(icdCodeStandardList, newTableName, userId);

			System.out.println("table created");
			
			codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(), "ICD Data Verification Table Created",
					"ICD Data Verification Table Created Successfully", globalUserId);

		} catch (IOException e) {
			codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(),
					"ICD Data Verification Table Creation Failed", "ICD Data Verification Table Creation Failed",
					globalUserId);
			
			System.err.println("An error occurred: " + e.getMessage());
			e.printStackTrace();

		}
		// return icdSyncResults;
	}

	@Transactional
	public void saveIcdCodeStandardList(List<IcdCodeStandard> icdCodeStandardList, String newTableName, int userId) {
		try {

			globalUserId = userId;
			truncateTable("icd_standard_versions");
			dropTable(newTableName);
			System.out.println("Before Saving");
			icdCodeStandardRepository.saveAll(icdCodeStandardList);
			createNewTableFromExisting(newTableName);
			System.out.println("After Saving");
			updateTempStatusOfCodeStandardFile("Dump Table Created");
			codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(), "Dump Table Created",
					"Dump Table Created Successfully", globalUserId);
			System.out.println(
					"After Dump Table Status Updated" + metafile.getId() + ":" + dumpTableName + ":" + globalUserId);
			icdCodeStandardRepository.prepareIcdDataForVerification(metafile.getId(), dumpTableName, globalUserId);
			System.out.println("Data Prepared for Verification");
			updateTempStatusOfCodeStandardFile("ICD Verification Data Prepared");
			codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(), "ICD Verification Data Prepared",
					"ICD Verification Data Prepared Successfully", globalUserId);

//		truncateTable("icd_standard_versions");
		} catch (Exception e) {
			codeMaintenanceLogService.saveCodeMaintenanceLog(metafile.getId(), "ICD Verification Data Prepared Failed",
					"ICD Verification Data Preparation Failed", globalUserId);
			System.out.println(e.getMessage());

		}

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
	public String createNewTableFromExisting(String newTableName) {
		System.out.println("New Table Creating from version Table");
		String status = "success";
		try {
			System.out.println("Table have to created with dumpname");
			entityManager.createNativeQuery("CREATE TABLE " + newTableName + " AS SELECT * FROM icd_standard_versions")
					.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			status = "failed";
		}
		return status;
	}

	public String getTargetFilePathFromZipFolder(String zipFileName) {

		// Assuming this is target File Name
		// "icd102022/dot/icd10cm_2022/icd10cm_2022_tab.txt";

		String tempName = zipFileName.replace("icd10", "icd10cm_").replace(".zip", ""); // icd10cm_2022
		String targetFileName = zipFileName.replace(".zip", "") + "/" + "dot/" + tempName + "/" + tempName + "_tab.txt";
		dumpTableName = tempName + "_tab";
		return targetFileName;

	}

	@Transactional
	public void updateTempStatusOfCodeStandardFile(String statusValue) {

		metafile.setCurrentStatus(statusValue);
		Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
		// System.out.println(newModifiedDate.toString());
		metafile.setModifiedDate(newModifiedDate);
		codeStandardFileRepository.save(metafile);
	}

//	@Transactional
//	public void test() {
//		icdCodeStandardRepository.prepareIcdDataForVerification(33, "icd10cm_2022_tab", 2);
//	}
//	

	@Transactional
	public Page<Icd10DataVerificationFile> getDataVerificationFileDetails(Pageable pageable) {

		return icd10DataVerificationFileRepository.findAll(pageable);
	}

	@Transactional
	public IcdSyncResults getIcdSyncResults(int id, int userId) {
		globalUserId = userId;
		IcdSyncResults icdSyncResults = new IcdSyncResults();
		String fileName;
		Integer fileId;
		Optional<CodeStandardFile> file = codeStandardFileRepository.findById(id);
		if (file.isPresent()) {

			CodeStandardFile codeStandardFile = file.get();
			fileId = codeStandardFile.getId();
			fileName = codeStandardFile.getFileName();

			String tempName = fileName.replace("icd10", "icd10cm_").replace(".zip", "");
			String taregtFileName = tempName + "_tab";

			icdSyncResults = icdSyncResultsRepo.icdCompareAndSyncTables(fileId, taregtFileName, globalUserId);
			// updateTempStatusOfCodeStandardFile("synching completed");
			codeMaintenanceLogService.saveCodeMaintenanceLog(codeStandardFile.getId(), "Icd10 Data Synching Completed",
					"Icd10 Data Synching Completed Successfully", globalUserId);
			System.out.println("Before truncateTable method call");
			truncateTable("icd_standard_versions");
			System.out.println("After truncateTable method call");
			codeStandardFile.setCurrentStatus("synching completed");
			codeStandardFile.setComments("ICD 10 File Proceessed Successfully");
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
			// System.out.println(newModifiedDate.toString());
			codeStandardFile.setModifiedDate(newModifiedDate);
			codeStandardFileRepository.save(codeStandardFile);
//		int addedRecords=icdSyncResults.getAdded_records();
//		int updatedRecords=icdSyncResults.getUpdated_records();
//		int deletedRecords=icdSyncResults.getDeleted_records();
			// storeRecordLogAfterSync(fileId,addedRecords,deletedRecords,updatedRecords);
			return icdSyncResults;
		} else {
			// Handle case where codeStandardFile is not found
			return icdSyncResults;
		}
	}

	@Transactional
	public List<Icd10DataVerificationFile> getIcdCodeOrDescOrstatus(Integer fileId, String icdCode, String codeDesc,
			String status) {

		return icd10DataVerificationFileRepository.getIcdCodeVerificationDetails(fileId, icdCode, codeDesc, status);
	}
	
	

	public void storeRecordLogAfterSync(Integer fileId, Integer addedRecords, Integer deletedRecords,
			Integer updatedRecords) {

		Icd10SyncDataRecords icd10SyncDataRecords = new Icd10SyncDataRecords();
		icd10SyncDataRecords.setFileId(fileId);
		icd10SyncDataRecords.setAddedRecords(addedRecords);
		icd10SyncDataRecords.setDeletedRecords(deletedRecords);
		icd10SyncDataRecords.setUpdatedRecords(updatedRecords);
		icd10SyncDataRecordsRepo.save(icd10SyncDataRecords);
	}

	@Transactional
	public Icd10SyncDataRecords getIcdSyncCounts(int fileId) {

		String syncStatus = "";
		Optional<CodeStandardFile> file = codeStandardFileRepository.findById(fileId);
		if (file.isPresent()) {

			CodeStandardFile metaFile = file.get();
			if (metaFile.getCurrentStatus().equalsIgnoreCase("ICD Verification Data Prepared")) {
				syncStatus = "Pre Sync";
			}
			if (metaFile.getCurrentStatus().equalsIgnoreCase("Synching Completed")) {
				syncStatus = "Post Sync";
			}

		}
		return icd10SyncDataRecordsRepo.findByStatus(syncStatus);
	}

	@Transactional
	public List<PostSyncIcdResults> getIcdPostSyncresults(int fileId, String status) {

		return icd10PostSyncResultsRepo.icdPostSyncDataResults(fileId, status);

	}

}