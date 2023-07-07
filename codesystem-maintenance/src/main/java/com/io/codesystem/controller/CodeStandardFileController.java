package com.io.codesystem.controller;

import java.sql.Date;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.io.codesystem.dto.model.CodeStandardFile;
import com.io.codesystem.dto.model.Icd10DataVerificationFile;
import com.io.codesystem.dto.model.IcdSyncResults;
import com.io.codesystem.service.CodeStandardFileService;
import com.io.codesystem.service.IcdCodeStandardService;
import com.io.codesystem.utility.JsonResponse;

@RestController
public class CodeStandardFileController {

	@Autowired
	private CodeStandardFileService codeStandardFileService;

	@Autowired
	private IcdCodeStandardService icdCodeStandardService;

	@PostMapping("/codeStandardFile/upload")
	public ResponseEntity<JsonResponse> uploadFile(@RequestParam("standard") String standard,
			@RequestParam("releaseVersion") String releaseVersion, 
			@RequestParam("releaseDate") Date releaseDate,
			@RequestParam("file") MultipartFile file) {

		String uploadStatus = codeStandardFileService.uploadCodeStandardFile(standard, releaseVersion, releaseDate,
				file);
		JsonResponse jsonResponse = new JsonResponse();
		HttpHeaders headers = new HttpHeaders();

		if (uploadStatus.equalsIgnoreCase("Success")) {
			jsonResponse.setStatus("FileUploaded Successfully");
			jsonResponse.setComments("FileUploaded Successfully");
			return new ResponseEntity<>(jsonResponse, headers, HttpStatus.OK);
		} else {
			jsonResponse.setStatus("File Uploading Failed");
			jsonResponse.setComments(uploadStatus);
			return new ResponseEntity<>(jsonResponse, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/s3bucket/files")
	public ResponseEntity<List<String>> getFileNamesFromS3Bucket(
			@RequestParam("bucketName") String bucketName,
			@RequestParam("rootFolderName") String rootFolderName) {

		List<String> filesList = codeStandardFileService.showFilesInS3Bucket(bucketName, rootFolderName);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(filesList, headers, HttpStatus.OK);

	}

//	@DeleteMapping("/s3bucket/files/delete")
//	public ResponseEntity<String> deleteFileInS3Bucket(@RequestParam("bucketName") String bucketName,
//			@RequestParam("filePath") String filePath) {
//
//	        String deleteStatus = codeStandardFileService.deleteFileInS3Bucket(bucketName, filePath);
// 			HttpHeaders headers = new HttpHeaders();
//			return new ResponseEntity<>(deleteStatus, headers, HttpStatus.OK);
//		
//	}....
	@DeleteMapping("/s3bucket/files/delete")
	public ResponseEntity<JsonResponse> deleteFileInS3Bucket(@RequestParam("bucketName") String bucketName,
			@RequestParam("filePath") String filePath) {
		JsonResponse jsonResponse = new JsonResponse();
		String deleteStatus = codeStandardFileService.deleteFileInS3Bucket(bucketName, filePath);
		HttpHeaders headers = new HttpHeaders();
		jsonResponse.setStatus(deleteStatus);
		jsonResponse.setComments("");
		return new ResponseEntity<>(jsonResponse, headers, HttpStatus.OK);

	}

	@GetMapping("/codestandardfile/list")
	public ResponseEntity<List<CodeStandardFile>> getCodeStandardFileDetails() {
		List<CodeStandardFile> files = codeStandardFileService.getCodeStandardFileDetails();
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(files, headers, HttpStatus.OK);

	}

	// Delete file from the front end
//    @DeleteMapping("/s3bucket/files/delete")
//	public ResponseEntity<String> getCodeStandardFileDetails(@RequestParam int fileId)
//	{
//	        String status = codeStandardFileService.deleteFileFromStandardTable(fileId);
// 			
// 			if (status.equalsIgnoreCase("Success")) {
// 				HttpHeaders headers = new HttpHeaders();
// 				return new ResponseEntity<>("File Deleted Successfully", headers, HttpStatus.OK);
// 			} else {
// 				HttpHeaders headers = new HttpHeaders();
// 				return new ResponseEntity<>(status, headers, HttpStatus.INTERNAL_SERVER_ERROR);
// 			}
//		
//	}
	@DeleteMapping("files/delete")
	public ResponseEntity<JsonResponse> getCodeStandardFileDetails(@RequestParam int fileId) {
		String deleteStatus = codeStandardFileService.deleteFileFromStandardTable(fileId);
		JsonResponse jsonResponse = new JsonResponse();
		HttpHeaders headers = new HttpHeaders();
		if (deleteStatus.equalsIgnoreCase("Success")) {

			jsonResponse.setStatus("File deleted successfully");
			jsonResponse.setComments("File deleted successfully");
			return new ResponseEntity<>(jsonResponse, headers, HttpStatus.OK);
		} else {
			jsonResponse.setStatus("File deleted failed");
			jsonResponse.setComments(deleteStatus);
			return new ResponseEntity<>(jsonResponse, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

//    @PostMapping("/codeStandardFile/process")
//    public ResponseEntity<String> processCodeStandardFileData(@RequestParam("id") int id) {
//    	
//  
//        String status = codeStandardFileService.processCodeStandardFileData(id);
//        
//        if (status.equalsIgnoreCase("Success")) {
//				HttpHeaders headers = new HttpHeaders();
//				return new ResponseEntity<>("File processed successfully", headers, HttpStatus.OK);
//			} else {
//				HttpHeaders headers = new HttpHeaders();
//				return new ResponseEntity<>(status, headers, HttpStatus.INTERNAL_SERVER_ERROR);
//			}
//    }
	@PostMapping("/codeStandardFile/process")
	public ResponseEntity<JsonResponse> processCodeStandardFileData(@RequestParam("id") int id) {

		String processStatus = codeStandardFileService.processCodeStandardFileData(id);
		JsonResponse jsonResponse = new JsonResponse();
		HttpHeaders headers = new HttpHeaders();
		if (processStatus.equalsIgnoreCase("Success")) {
			jsonResponse.setStatus("File Processed successfully");
			jsonResponse.setComments("File Processed successfully");
			return new ResponseEntity<>(jsonResponse, headers, HttpStatus.OK);
		} else {
			jsonResponse.setStatus("File Processed failed");
			jsonResponse.setComments(processStatus);
			return new ResponseEntity<>(jsonResponse, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/updates/verification")
	public ResponseEntity<List<Icd10DataVerificationFile>> getDataVerificationFileDetails() {
		List<Icd10DataVerificationFile> response = icdCodeStandardService.getDataVerificationFileDetails();
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping("/updates/synching")
	public ResponseEntity<IcdSyncResults> getIcdSyncResults(@RequestParam int id) {
		IcdSyncResults response = icdCodeStandardService.getIcdSyncResults(id);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}
}
