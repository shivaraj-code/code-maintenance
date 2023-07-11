
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.io.codesystem.dto.model.CodeStandardFile;
import com.io.codesystem.dto.model.Icd10DataVerificationFile;
import com.io.codesystem.dto.model.IcdSyncResults;
import com.io.codesystem.dto.model.MedicineDataVerificationFile;
import com.io.codesystem.dto.model.MedicineStandard;
import com.io.codesystem.dto.model.MedicineSyncResults;
import com.io.codesystem.service.CodeStandardFileService;
import com.io.codesystem.service.MedicineStandardService;
import com.io.codesystem.utility.JsonResponse;

@RestController
public class MedicineStandardFileController {

	@Autowired
	private CodeStandardFileService codeStandardFileService;
	@Autowired
	private MedicineStandardService medicineStandardService;

	@PostMapping("/medicineStandardFile/upload")
	public ResponseEntity<JsonResponse> uploadFile(
			
			@RequestParam("standard") String standard,
			@RequestParam("releaseVersion") String releaseVersion, 
			@RequestParam("releaseDate") Date releaseDate,
			@RequestParam("file") MultipartFile file) {

		String uploadStatus = codeStandardFileService.uploadCodeStandardFile(standard, 
				releaseVersion,
				releaseDate,
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

	@GetMapping("/medicinestandardfile/list")
	public ResponseEntity<List<CodeStandardFile>> getCodeStandardFileDetails() {
		List<CodeStandardFile> files = codeStandardFileService.getCodeStandardFileDetails();
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(files, headers, HttpStatus.OK);

	}
	
	@DeleteMapping("/medicinefiles/delete")
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
	
	@PostMapping("/medicineStandardFile/process")
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
	public ResponseEntity<List<MedicineDataVerificationFile>> getDataVerificationFileDetails() {
		List<MedicineDataVerificationFile> response = medicineStandardService.getDataVerificationFileDetails();
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping("/updates/synching")
	public ResponseEntity<MedicineSyncResults> getMedicineSyncResults(@RequestParam int id) {
		MedicineSyncResults response = medicineStandardService.getMedicineSyncResults(id);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}
	
}
