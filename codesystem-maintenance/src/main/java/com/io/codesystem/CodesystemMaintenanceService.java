package com.io.codesystem;

import java.io.IOException;

public abstract class CodesystemMaintenanceService {
	
	
	public abstract String fileReadingFromS3Bucket(String bucketName, String filename, String sourceFolderPrefix, String destinationFolderPrefix);

    public abstract String fileProcessAndInsertData(String zipFilePath,String targetFolderPath, String targetFileName)throws IOException;

    public abstract void analysis();

    public abstract void synching();

    public abstract void verification();

    public abstract void complete();
    
  
}
