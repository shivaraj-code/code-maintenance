package com.io.codesystem.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.io.codesystem.dto.model.CodeMaintenanceLog;
import com.io.codesystem.repo.CodeMaintenaceLogRepository;

@Service
public class CodeMaintenanceLogService {
	
	@Autowired
	CodeMaintenaceLogRepository codeMaintenaceLogRepository;
	
	public CodeMaintenanceLog saveCodeMaintenanceLog(int fileId, String eventType, String eventDesc, int userId) {
		CodeMaintenanceLog codeMaintenanceLog = new CodeMaintenanceLog();
		codeMaintenanceLog.setEventType(eventType);
		codeMaintenanceLog.setEventDesc(eventDesc);
		codeMaintenanceLog.setFileId(fileId);
		codeMaintenanceLog.setUserId(userId);
		codeMaintenanceLog.setInsertedDate(Timestamp.valueOf(LocalDateTime.now()));
		
		return codeMaintenaceLogRepository.save(codeMaintenanceLog);
	}

}
