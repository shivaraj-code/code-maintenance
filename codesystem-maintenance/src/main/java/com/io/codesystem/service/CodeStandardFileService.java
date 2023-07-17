package com.io.codesystem.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.io.codesystem.dto.model.CodeStandardFile;
import com.io.codesystem.repo.CodeStandardFileRepository;
import com.io.codesystem.utility.S3Utility;

@Service
public class CodeStandardFileService {

	private static final CodeStandardFileService medicineStandardService = null;

	@Autowired
	private S3Utility s3Utility;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	@Value("${aws.s3.root-folder}")
	private String rootFolderName;

	@Value("${aws.s3.upload-folder}")
	private String uploadFolderName;

	@Value("${aws.s3.inprocess-folder}")
	private String inprocessFolderName;

	@Value("${aws.s3.processed-folder}")
	private String processedFolderName;

	@Value("${aws.s3.icd-folder}")
	private String icdFolderName;

	@Value("${aws.s3.snomed-folder}")
	private String snomedFolderName;

	@Value("${aws.s3.medicine-folder}")
	private String medicineFolderName;

	@Autowired
	CodeStandardFileRepository codeStandardFileRepository;

	@Autowired
	IcdCodeStandardService icdCodeStandardService;
	
	@Autowired
	MedicineStandardService medicinStandardService;
	
	@Autowired
	CodeMaintenanceLogService codeMaintenanceLogService;
	
	int globalUserId=0;

	public String uploadCodeStandardFile(String standard, 
			String releaseVersion, Date releaseDate, int userId, MultipartFile file) {

		String fileName = file.getOriginalFilename();
		String targetCodeStandardFolder = getCodeStandardTargetFolderName(standard);
		String targetFolderPath = rootFolderName + "/" + uploadFolderName + "/" + targetCodeStandardFolder + "/";
		String targetFileName = fileName;

        String fileExistigStatus=checkFileAlreadyExistsInAnyTargetCodeFolders(targetFileName, targetCodeStandardFolder);
		String fileUploadStatus="";
		globalUserId = userId;
		try {
			
	if(fileExistigStatus.equalsIgnoreCase("Success"))
	{
			s3Utility.saveFile(bucketName, targetFolderPath + targetFileName, file.getInputStream());
			CodeStandardFile codeStandardFile = new CodeStandardFile();
			
			codeStandardFile.setCodeStandard(standard);
			codeStandardFile.setFileName(targetFileName);
			codeStandardFile.setFilePath(targetFolderPath+targetFileName);
			codeStandardFile.setReleaseVersion(releaseVersion);
			codeStandardFile.setReleaseDate(releaseDate);
			codeStandardFile.setProcessedStatus("Uploaded");
			codeStandardFile.setCurrentStatus("File Uploaded Successfully");
			codeStandardFile.setActive(1);
            codeStandardFile.setUserId(globalUserId);
//            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			codeStandardFile.setInsertedDate(Timestamp.valueOf(LocalDateTime.now()));
			codeStandardFile.setSource("AMA");
			codeStandardFile.setStatus("Success");
//			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			codeStandardFile.setModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
			codeStandardFile.setModifiedUserId(globalUserId);
			codeStandardFile.setComments("");
			saveUploadedFileDetails(codeStandardFile);
			
			fileUploadStatus="Success";
	}else {
		return fileExistigStatus;
	}
		} catch (IOException e) {
			e.printStackTrace();
			fileUploadStatus = "File uploading failed with reason:" + e.getMessage();
		}

		return fileUploadStatus;
//		
	}

	public String getCodeStandardTargetFolderName(String codeStandard) {

		switch (codeStandard) {
		case "icd10":
			return icdFolderName;

		case "snomed":
			return snomedFolderName;

		case "medicine":
			return medicineFolderName;

		default:
			return "invalid code standard name";

		}
	}

	public List<String> showFilesInS3Bucket(String bucketName, String rootFolderName) {

		List<String> list = new LinkedList<>();
		try {
			List<S3ObjectSummary> s3FilesSummary = s3Utility.getS3Files(bucketName, rootFolderName);
			for (S3ObjectSummary summary : s3FilesSummary) {

				list.add(summary.getKey().toString());
			}
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return list;
	}

	public String deleteFileInS3Bucket(String bucketName, String filePath) {

		try {
			s3Utility.delete(bucketName, filePath);

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			return "File Delete Failed with Key:" + filePath;
		}
		return "File Deleted Successfully with Key:" + filePath;
	}

	@Transactional
	public String saveUploadedFileDetails(CodeStandardFile file) {

		try {
			codeStandardFileRepository.save(file);
			codeMaintenanceLogService.saveCodeMaintenanceLog
			   (file.getId(), "File Uploading", "File Uploaded Successfully", globalUserId);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			return "Failed: Uploaded File Details Saving Failed";
		}
		return "Success: Uploaded File Details Saved Successfully";
	}

//	public List<CodeStandardFile> getCodeStandardFileDetails()
//	{
//		
//	   return codeStandardFileRepository.findAll();
//	}
	public Page<CodeStandardFile> getCodeStandardFileDetails(Pageable pageable)
	{
		
	   return codeStandardFileRepository.findAll(pageable);
	}
	/*
	 * This will check if uploading file already exists or not in any of Code Type
	 * folder Ex: icd/upload, icd/inprocess, icd/processed etc
	 */
	public String checkFileAlreadyExistsInAnyTargetCodeFolders(String fileName, String targetCodeStandardFolder) {

		String uploadFolderPath = rootFolderName + "/" + uploadFolderName + "/" + targetCodeStandardFolder + "/"
				+ fileName;
		String inprocessFolderPath = rootFolderName + "/" + inprocessFolderName + "/" + targetCodeStandardFolder + "/"
				+ fileName;
		String processedFolderPath = rootFolderName + "/" + processedFolderName + "/" + targetCodeStandardFolder + "/"
				+ fileName;

		if (s3Utility.isExist(bucketName, uploadFolderPath)) {
			return "File uploading failed due to Same File already exists in S3 Bucket, Path:\"" + uploadFolderPath
					+ "\". Please delete existing file, If you want to upload new version of same file";
		}
		if (s3Utility.isExist(bucketName, inprocessFolderPath)) {
			return "File uploading failed due to Same File already exists in S3 Bucket Path:\"" + inprocessFolderPath
					+ "\". You cant upload same file, if it is already in In-Process Folder";
		}
		if (s3Utility.isExist(bucketName, processedFolderPath)) {
			return "File uploading failed due to Same File already exists in S3 Bucket Path:\"" + processedFolderPath
					+ "\". You cant upload same file, if it is already in Processed Folder";
		}
		return "Success";
	}

	
	public String deleteFileFromStandardTable(int fileId, int userId) {
		
		String status="Success";
		try {
		
		Optional<CodeStandardFile> file= codeStandardFileRepository.findById(fileId);
		codeStandardFileRepository.deleteById(fileId);
		deleteFileInS3Bucket(bucketName, file.get().getFilePath());
		
		codeMaintenanceLogService.saveCodeMaintenanceLog(fileId,
				    "File Deletion", "File Deleted Successfully", userId);
		
		}catch(Exception e) {
			codeMaintenanceLogService.saveCodeMaintenanceLog(fileId,
				    "File Deletion", "File Deletion Failed", globalUserId);
			status="File Deletion Failed:"+e.getMessage();
		}
		return status;
	}
	public String processCodeStandardFileData(int fileId, int userId) {
		
		String status="Success";
		
		try {
			Optional<CodeStandardFile> file = codeStandardFileRepository.findById(fileId);
			if (file.isPresent()) {
				CodeStandardFile fileData = file.get();
				fileData.setProcessedStatus("InProcess");
				codeStandardFileRepository.save(fileData);

				switch (fileData.getCodeStandard()) {

				case "icd10":
					icdCodeStandardService.processCodeStandardFileData(fileData, bucketName, userId);
					//icdCodeStandardService.test();
					break;

				case "medicine":
					medicinStandardService.processCodeStandardFileData(fileData, bucketName, userId);

					break;

				default:
					status = "Unknown Code Standard Name for Processing the File";
				}
			} else {
				return "There is no record with given File Id or File is not in Uploaded Status";
			}

		} catch (Exception e) {
			status = "File Process Failed:" + e.getMessage();
		}
		return status;

}

	
}
