package com.io.codesystem.controller;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.io.codesystem.dto.model.Icd10SyncDataRecords;
import com.io.codesystem.dto.model.IcdSyncResults;
import com.io.codesystem.dto.model.MedicineDataVerificationFile;
import com.io.codesystem.dto.model.MedicineSyncDataRecords;
import com.io.codesystem.dto.model.MedicineSyncResults;
import com.io.codesystem.dto.model.PostSyncIcdResults;
import com.io.codesystem.dto.model.PostSyncMedicineResults;
import com.io.codesystem.service.CodeStandardFileService;
import com.io.codesystem.service.IcdCodeStandardService;
import com.io.codesystem.service.MedicineStandardService;
import com.io.codesystem.utility.JsonResponse;

import ch.qos.logback.core.status.Status;

@RestController
public class CodeStandardFileController {

	@Autowired
	private CodeStandardFileService codeStandardFileService;

	@Autowired
	private IcdCodeStandardService icdCodeStandardService;

	@Autowired
	private MedicineStandardService medicineStandardService;

	@PostMapping("/codeStandardFile/upload")
	public ResponseEntity<JsonResponse> uploadFile(@RequestParam("standard") String standard,
			@RequestParam("releaseVersion") String releaseVersion, @RequestParam("releaseDate") Date releaseDate,
			@RequestParam("userId") int userId, @RequestParam("file") MultipartFile file) {

		String uploadStatus = codeStandardFileService.uploadCodeStandardFile(standard, releaseVersion, releaseDate,
				userId, file);
		JsonResponse jsonResponse = new JsonResponse();
		HttpHeaders headers = new HttpHeaders();

		if (uploadStatus.equalsIgnoreCase("Success")) {
			jsonResponse.setStatus("File Uploaded Successfully");
			jsonResponse.setComments("File Uploaded Successfully");
			return new ResponseEntity<>(jsonResponse, headers, HttpStatus.OK);
		} else {
			jsonResponse.setStatus("File Uploading Failed");
			jsonResponse.setComments(uploadStatus);
			return new ResponseEntity<>(jsonResponse, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/s3bucket/files")
	public ResponseEntity<List<String>> getFileNamesFromS3Bucket(@RequestParam("bucketName") String bucketName,
			@RequestParam("rootFolderName") String rootFolderName) {

		List<String> filesList = codeStandardFileService.showFilesInS3Bucket(bucketName, rootFolderName);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(filesList, headers, HttpStatus.OK);

	}

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
	public ResponseEntity<Page<CodeStandardFile>> getCodeStandardFileDetails(Pageable pageable) {
		Page<CodeStandardFile> page = codeStandardFileService.getCodeStandardFileDetails(pageable);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(page, headers, HttpStatus.OK);

	}

	@DeleteMapping("files/delete")
	public ResponseEntity<JsonResponse> getCodeStandardFileDetails(@RequestParam int fileId, @RequestParam int userId) {
		String deleteStatus = codeStandardFileService.deleteFileFromStandardTable(fileId, userId);
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

	@PostMapping("/codeStandardFile/process")
	public ResponseEntity<JsonResponse> processCodeStandardFileData(@RequestParam("fileId") int fileId,
			@RequestParam("userId") int userId) {

		String processStatus = codeStandardFileService.processCodeStandardFileData(fileId, userId);
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

	@GetMapping("/updates/synching")
	public ResponseEntity<IcdSyncResults> getIcdSyncResults(@RequestParam int id, @RequestParam int userId) {
		IcdSyncResults response = icdCodeStandardService.getIcdSyncResults(id, userId);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	// Medicinces Synching
	@GetMapping("/updates/medicines/synching")
	public ResponseEntity<MedicineSyncResults> getMedicineSyncResults(@RequestParam int id, @RequestParam int userId) {
		MedicineSyncResults response = medicineStandardService.getMedicineSyncResults(id, userId);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping(path = "/verification/icd")
	public ResponseEntity<List<Icd10DataVerificationFile>> getIcdCodeOrDescOrstatus(@RequestParam Integer fileId,
			@RequestParam String icdCode, @RequestParam String codeDesc, @RequestParam String status) {
		HttpHeaders headers = new HttpHeaders();
		List<Icd10DataVerificationFile> response = icdCodeStandardService.getIcdCodeOrDescOrstatus(fileId, icdCode,
				codeDesc, status);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	// medicine verification
	/*
	 * @GetMapping(path = "/verification/medicine") public
	 * ResponseEntity<List<MedicineDataVerificationFile>> getNameorNDC(@RequestParam
	 * int fileId,
	 * 
	 * @RequestParam String ndc,
	 * 
	 * @RequestParam String name,
	 * 
	 * @RequestParam String status) { HttpHeaders headers = new HttpHeaders();
	 * List<MedicineDataVerificationFile> response =
	 * medicineStandardService.getNameorNDC(fileId, ndc, name, status); return new
	 * ResponseEntity<>(response, headers, HttpStatus.OK); }
	 */

	@GetMapping(path = "/verification/medicine")
	public ResponseEntity<Page<MedicineDataVerificationFile>> getNameorNDC(@RequestParam int fileId,
			@RequestParam String ndc,
			@RequestParam String name,
			@RequestParam String status,
			@RequestParam(defaultValue = "10") int pageSize,
	        @RequestParam(defaultValue = "0") int pageNumber) {
		HttpHeaders headers = new HttpHeaders();
		Page<MedicineDataVerificationFile> response = medicineStandardService.getNameorNDC(fileId, ndc, name, status,pageSize, pageNumber);
		return new ResponseEntity<>(response,headers, HttpStatus.OK);
	}

	@GetMapping("pre/post/synccounts/icd")
	public ResponseEntity<Icd10SyncDataRecords> getIcdPreSyncCounts(@RequestParam int fileId) {
		Icd10SyncDataRecords response = icdCodeStandardService.getIcdSyncCounts(fileId);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	// medicine synccount
	@GetMapping("pre/post/synccounts/medicine")
	public ResponseEntity<MedicineSyncDataRecords> getMedicinePreSyncCounts(@RequestParam int fileId) {
		MedicineSyncDataRecords response = medicineStandardService.getMedicineSyncCounts(fileId);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping("/postsyncresults/icd")
	public ResponseEntity<List<PostSyncIcdResults>> getIcdPostSyncresults(@RequestParam int fileId,
			@RequestParam String status) {
		List<PostSyncIcdResults> response = icdCodeStandardService.getIcdPostSyncresults(fileId, status);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	// medicine syncresults
	@GetMapping("/postsyncresults/medicine")
	public ResponseEntity<List<PostSyncMedicineResults>> getMedicinePostSyncresults(@RequestParam int fileId,
			@RequestParam String status) {
		List<PostSyncMedicineResults> response = medicineStandardService.getMedicineSyncResults(fileId, status);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}
}
