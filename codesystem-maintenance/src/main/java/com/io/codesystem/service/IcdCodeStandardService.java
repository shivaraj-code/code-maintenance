package com.io.codesystem.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.io.codesystem.dto.model.CodeStandardFile;
import com.io.codesystem.dto.model.Icd10DataVerificationFile;
import com.io.codesystem.dto.model.IcdCodeStandard;
import com.io.codesystem.dto.model.IcdSyncResults;
import com.io.codesystem.repo.CodeStandardFileRepository;
import com.io.codesystem.repo.Icd10DataVerificationFileRepository;
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

	@PersistenceContext
	private EntityManager entityManager;

	String dumpTableName = "icd10_dump";
	CodeStandardFile metafile = null;

	int userId = 2;

	@Transactional
	public String processCodeStandardFileData(CodeStandardFile file, String bucketName) throws IOException {

		if (s3Utility.isExist(bucketName, file.getFilePath())) {

			try (InputStream inputStream = s3Utility.getS3File(bucketName, file.getFilePath())) {

				metafile = file;
				String targetFileName = getTargetFilePathFromZipFolder(file.getFileName());
				fileProcessAndInsertData(inputStream, targetFileName);
			}

		}
		return "success";
	}

	@Transactional
	public void fileProcessAndInsertData(InputStream zipInputStream, String targetFileName) throws IOException {

		File targetFile = null;
		System.out.println("Zip file processing started");
		// String status = "failed";

		try (ZipInputStream zip = new ZipInputStream(zipInputStream)) {
			ZipEntry entry;
			System.out.println("************" + zip.getNextEntry().getName());

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

						updateTempStatusOfCodeStandardFile("Reading of File Content Completed");
						// Process and insert the data
						System.out.println("Dump Table Name:" + dumpTableName);
						insertDataIntoDatabase(targetFile, dumpTableName);
					}
					break;
				}

			}
		}
	}

	@Transactional
	public void insertDataIntoDatabase(File file, String newTableName) {

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

			saveIcdCodeStandardList(icdCodeStandardList, newTableName);

			System.out.println("table created");

		} catch (IOException e) {

			System.err.println("An error occurred: " + e.getMessage());
			e.printStackTrace();

		}
		// return icdSyncResults;
	}

	@Transactional
	public void saveIcdCodeStandardList(List<IcdCodeStandard> icdCodeStandardList, String newTableName) {
		try {
			truncateTable("icd_standard_versions");
			dropTable(newTableName);
			System.out.println("Before Saving");
			icdCodeStandardRepository.saveAll(icdCodeStandardList);
			createNewTableFromExisting(newTableName);
			System.out.println("After Saving");
			updateTempStatusOfCodeStandardFile("Dump Table Created");
			System.out
					.println("After Dump Table Status Updated" + metafile.getId() + ":" + dumpTableName + ":" + userId);
			icdCodeStandardRepository.prepareIcdDataForVerification(metafile.getId(), dumpTableName, userId);
			System.out.println("Data Prepared for Verification");
			updateTempStatusOfCodeStandardFile("ICD Verification Data Prepared");
			truncateTable("icd_standard_versions");
		} catch (Exception e) {
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

		metafile.setTempStatus(statusValue);
		codeStandardFileRepository.save(metafile);
	}

//	@Transactional
//	public void test() {
//		icdCodeStandardRepository.prepareIcdDataForVerification(33, "icd10cm_2022_tab", 2);
//	}
//	
	public List<Icd10DataVerificationFile> getDataVerificationFileDetails() {

		return icd10DataVerificationFileRepository.findAll();
	}

	public IcdSyncResults getIcdSyncResults(int id) {
		IcdSyncResults icdSyncResults = new IcdSyncResults();
		String fileName;
		Integer fileId;
		Optional<CodeStandardFile> file = codeStandardFileRepository.findById(id);
		if (file.isPresent()) {
			CodeStandardFile codeStandardFile = file.get();
			fileId = codeStandardFile.getId();
			fileName = codeStandardFile.getFileName();
			icdSyncResults = icdSyncResultsRepo.icdCompareAndSyncTables(fileId, fileName, userId);
			updateTempStatusOfCodeStandardFile("synching completed");
			return icdSyncResults;
		} else {
			// Handle case where codeStandardFile is not found
			return icdSyncResults;
		}
	}

}